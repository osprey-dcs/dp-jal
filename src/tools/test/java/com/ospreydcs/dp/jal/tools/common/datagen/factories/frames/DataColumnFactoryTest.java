/*
 * Project: dp-jal
 * File:	DataColumnFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	DataColumnFactoryTest
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
 * @since Nov 20, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.lib.ScalarFactoryLib;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.lib.TensorFactoryLib;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorUtility;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>DataColumnFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 20, 2025
 *
 */
public class DataColumnFactoryTest {

    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    
    //
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#from(java.lang.String, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final String            strColNm = "PV1";
//        final int               szCol = 100;
        final ScalarFactory     facVals = ScalarFactoryLib.BOOLEAN_ALT.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        
        try {
            // Create the column factory and check configuration
//            DataColumnFactory   facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            DataColumnFactory   facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#from(java.lang.String, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testFromFail() {
        
        // Test Parameters
        final String            strColNm = "PV1";
//        final int               szCol = 0;
        final ScalarFactory     facVals = null;
        
        try {
            // Attempt column factory creation with bad arguments
            @SuppressWarnings("unused")
//            DataColumnFactory   facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            DataColumnFactory   facTest = DataColumnFactory.from(strColNm, facVals);
        
            Assert.fail("DataColumnFactory creation/construction should have thrown IllegalArgumentException: scalar factory = " + facVals);
            
        // Bad argument - should throw exception
        } catch (Exception e) {
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#DataColumnFactory(java.lang.String, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testDataColumnFactory() {
        
        // Test Parameters
        final String            strColNm = "PV1";
//        final int               szCol = 100;
        final ScalarFactory     facVals = ScalarFactoryLib.DEFAULT.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        
        try {
            // Construct the column factory and check configuration
//            DataColumnFactory   facTest = new DataColumnFactory(strColNm, szCol, facVals);
            DataColumnFactory   facTest = new DataColumnFactory(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#getColumnName()}.
//     */
//    @Test
//    public final void testGetColumnName() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#getColumnSize()}.
//     */
//    @Test
//    public final void testGetColumnSize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#getColumnType()}.
//     */
//    @Test
//    public final void testGetColumnType() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#nextColumn()}.
     */
    @Test
    public final void testBuildBooleanAlt() {
        
        // Test Parameters
        final String            strColNm = "PV1";
        final int               szCol = 10;
        final ScalarFactory     facVals = ScalarFactoryLib.BOOLEAN_ALT.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        
        final int               cntCols = 5;

        // Create the column factory and check configuration
        DataColumnFactory   facTest;
        try {
//            facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);
        System.out.println("  Column count: " + cntCols);

        // Create some columns and check configuration and values
        List<Boolean>           lstVals = new ArrayList<>(szCol);
        
        Boolean bolCurr = false;
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = facTest.nextColumn(szCol);
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof Boolean bolVal) {
                    // Check value
                    Assert.assertEquals("Column #" + iCol + " value #" + iVal + " was bad.", bolCurr, bolVal);
                    lstVals.add(bolVal);

                    // Advance current test value
                    bolCurr = bolCurr ? false : true;
                    
                } else
                    Assert.fail("Value of column #" + iCol + " value #" + iVal + " was not Boolean.");
            }
            
            // Print out column values and clear value list
            System.out.println("  Column #" + iCol + " values: " + lstVals);
            lstVals.clear();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#nextColumn()}.
     */
    @Test
    public final void testBuildIntegerIncr1() {
        
        // Test Parameters
        final String            strColNm = "IntIncr1Test";
        final int               szCol = 25;
        final ScalarFactory     facVals = ScalarFactoryLib.INTEGER_INCR_1.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        
        final int               cntCols = 10;

        // Create the column factory and check configuration
        DataColumnFactory   facTest;
        try {
//            facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);
        System.out.println("  Column count: " + cntCols);

        // Create some columns and check configuration and values
        List<Integer>           lstVals = new ArrayList<>(szCol);
        
        Integer intCurr = 0;
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = facTest.nextColumn(szCol);
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof Integer intVal) {
                    // Check value
                    Assert.assertEquals("Column #" + iCol + " value #" + iVal + " was bad.", intCurr, intVal);
                    lstVals.add(intVal);
                    
                    // Advance current test value
                    intCurr++;
                    
                } else
                    Assert.fail("Value of column #" + iCol + " value #" + iVal + " was not Integer.");
            }
            
            // Print out column values and clear value list
            System.out.println("  Column #" + iCol + " values: " + lstVals);
            lstVals.clear();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#nextColumn()}.
     */
    @Test
    public final void testBuildDoubleIncr1em16() {
        
        // Test Parameters
        final String            strColNm = "DblIncrTest";
        final int               szCol = 10;
        final ScalarFactoryLib   enmFac = ScalarFactoryLib.DOUBLE_INCR_EM16;
        final ScalarFactory     facVals = enmFac.newFactory();
        final DpSupportedType   enmType = enmFac.getDpType();
        final Double            dblIncr = enmFac.getConfiguration().numIncr().doubleValue();
        final Double            dblSeed = (double) enmFac.getConfiguration().lngSeed();
        final int               cntCols = 10;

        // Create the column factory and check configuration
        DataColumnFactory   facTest;
        try {
//            facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);
        System.out.println("  Column count: " + cntCols);

        // Create some columns and check configuration and values
        List<Double>           lstVals = new ArrayList<>(szCol);
        
        Double dblCurr = dblSeed;
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = facTest.nextColumn(szCol);
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof Double dblVal) {
                    // Check value
                    Assert.assertEquals("Column #" + iCol + " value #" + iVal + " was bad.", dblCurr, dblVal);
                    lstVals.add(dblVal);
                    
                    // Advance current test value
                    dblCurr += dblIncr;
                    
                } else
                    Assert.fail("Value of column #" + iCol + " value #" + iVal + " was not Double.");
            }
            
            // Print out column values and clear value list
            System.out.println("  Column #" + iCol + " values: " + lstVals);
            lstVals.clear();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#nextColumn()}.
     */
    @Test
    public final void testBuildDoubleRnd() {
        
        // Test Parameters
        final String            strColNm = "DblRndTest";
        final int               szCol = 10;
        final ScalarFactoryLib   enmFac = ScalarFactoryLib.DOUBLE_RND;
        final ScalarFactory     facVals = enmFac.newFactory();
        final DpSupportedType   enmType = enmFac.getDpType();
        final int               cntCols = 10;

        // Create the column factory and check configuration
        DataColumnFactory   facTest;
        try {
//            facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);
        System.out.println("  Column count: " + cntCols);

        // Create some columns and check configuration and values
        List<Double>           lstVals = new ArrayList<>(szCol);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = facTest.nextColumn(szCol);
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof Double dblVal) {
                    // Check value
                    if (dblVal < 0 || dblVal > 1)
                        Assert.fail("Random double value " + dblVal + " not in [0,1].");
                    lstVals.add(dblVal);
                    
                } else
                    Assert.fail("Value of column #" + iCol + " value #" + iVal + " was not Double.");
            }
            
            // Print out column values and clear value list
            System.out.println("  Column #" + iCol + " values: " + lstVals);
            lstVals.clear();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.DataColumnFactory#nextColumn()}.
     */
    @Test
    public final void testBuildTensorIntegerIncr1() {
        
        // Test Parameters
        final String            strColNm = "TensorIntTest";
        final int               szCol = 10;
        final int[]             shape = { 1, 2, 3 };
        final TensorFactoryLib enmFac = TensorFactoryLib.INTEGER_INCR_1;
        
        final TensorFactory     facVals = enmFac.newFactory(shape);
        final JalScalarType     enmJalType = enmFac.getJalScalarType();
        final DpSupportedType   enmDpType = facVals.getDatumType();
        
        final int               cntCols = 10;

        // Create the column factory and check configuration
        DataColumnFactory   facTest;
        try {
//            facTest = DataColumnFactory.from(strColNm, szCol, facVals);
            facTest = DataColumnFactory.from(strColNm, facVals);
            
            Assert.assertEquals(strColNm, facTest.getColumnName());
//            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmDpType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmDpType);
        System.out.println("  Column count: " + cntCols);

        // Create some columns and check configuration and values
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = facTest.nextColumn(szCol);
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmDpType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof ArrayList vecTen) {
                    // Check value
                    int[]   arrShape = TensorUtility.tensorShape(vecTen);
                    Object  objElem = TensorUtility.extractFirstElement(vecTen);
                    
                    Assert.assertTrue("Column #" + iCol + " value #" + iVal + " wrong shape.", Arrays.equals(shape, arrShape));
                    Assert.assertTrue("Column #" + iCol + " value #" + iVal + " is not " + enmJalType, TensorUtility.isElementValueOfType(enmJalType, objElem) );
                } else
                    Assert.fail("Value of column #" + iCol + " value #" + iVal + " was not Double.");
            }
            
        }
    }

}
