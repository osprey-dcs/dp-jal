/*
 * Project: dp-jal
 * File:	DataColumnsFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.frames
 * Type: 	DataColumnsFactoryTest
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
 * @since Nov 21, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.frames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.ScalarFactoryEnum;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.TensorFactoryEnum;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorUtility;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.values.TensorFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>DataColumnsFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 21, 2025
 *
 */
public class DataColumnsFactoryTest {

    
    //
    // Test Resources
    //
    
    /** Collection of test column names */
    public static final Set<String>     SET_COL_NMS_0 = Set.of(); 
    
    /** Collection of test column names */
    public static final Set<String>     SET_COL_NMS_1 = Set.of("PV1"); 
    
    /** Collection of test column names */
    public static final Set<String>     SET_COL_NMS_3 = Set.of("PV1", "PV2", "PV3"); 
    
    /** Collection of test column names */
    public static final Set<String>     SET_COL_NMS_5 = Set.of("PV1", "PV2", "PV3", "PV4", "PV5"); 

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#from(java.util.Set, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testFromFail() {
        
        // Test Parameters
        final Set<String>           setColNms = SET_COL_NMS_0;
        final int                   szCol = 100;
        final ScalarFactoryEnum     enmFac = ScalarFactoryEnum.DEFAULT;
        
        final ScalarFactory         facVals = enmFac.newFactory();

        // Attempt new columns factory creation with bad arguments 
        try {
            @SuppressWarnings("unused")
            DataColumnsFactory  facTest = DataColumnsFactory.from(setColNms, szCol, facVals);

            Assert.fail("DataColumnsFactory creation success with bad arguments.");
            
        } catch (Exception e) {
            // We should land here
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("  Expected exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#from(java.util.Set, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final Set<String>           setColNms = SET_COL_NMS_1;
        final int                   szCol = 100;
        final ScalarFactoryEnum     enmFac = ScalarFactoryEnum.BOOLEAN_ALT;

        final int                   cntCols = setColNms.size();
        final DpSupportedType       enmType = enmFac.getDpType();
        final ScalarFactory         facVals = enmFac.newFactory();

        // Create new columns factory and check configuration
        try {
            DataColumnsFactory  facTest = DataColumnsFactory.from(setColNms, szCol, facVals);
            
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#DataColumnsFactory(java.util.Set, int, com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory)}.
     */
    @Test
    public final void testDataColumnsFactory() {
        
        // Test Parameters
        final Set<String>           setColNms = SET_COL_NMS_3;
        final int                   szCol = 100;
        final ScalarFactoryEnum     enmFac = ScalarFactoryEnum.INTEGER_INCR_1;

        final int                   cntCols = setColNms.size();
        final DpSupportedType       enmType = enmFac.getDpType();
        final ScalarFactory         facVals = enmFac.newFactory();

        // Construct new columns factory and check configuration
        try {
            DataColumnsFactory  facTest = new DataColumnsFactory(setColNms, szCol, facVals);
            
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsFactory construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#getColumnCount()}.
//     */
//    @Test
//    public final void testGetColumnCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#getColumnNames()}.
//     */
//    @Test
//    public final void testGetColumnNames() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#getColumnSize()}.
//     */
//    @Test
//    public final void testGetColumnSize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#getColumnType()}.
//     */
//    @Test
//    public final void testGetColumnType() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#build()}.
     */
    @Test
    public final void testBuildIntegerIncr1() {
        
        // Test Parameters
        final Set<String>       setColNms = SET_COL_NMS_3;
        final int               szCol = 25;
        final ScalarFactoryEnum enmFac = ScalarFactoryEnum.INTEGER_INCR_1;
        
        final int               cntCols = setColNms.size();
        final ScalarFactory     facVals = ScalarFactoryEnum.INTEGER_INCR_1.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        final int               intSeed = Math.toIntExact( enmFac.getConfiguration().seed() );
        final int               intIncr = facVals.getConfiguration().increment().intValue();
        

        // Create the column factory and check configuration
        DataColumnsFactory   facTest;
        try {
            facTest = DataColumnsFactory.from(setColNms, szCol, facVals);
            
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column names: " + setColNms);
        System.out.println("  Column count: " + cntCols);
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);

        // Create a column and check configuration and values
        List<Integer>           lstVals = new ArrayList<>(szCol);
        
        ArrayList<IDataColumn<Object>>  vecCols = facTest.build();
        Iterator<String>                itrColNms = setColNms.iterator();
        Integer                         intCurr = intSeed;
        for (int iCol=0; iCol<cntCols; iCol++) {
            IDataColumn<Object>     col = vecCols.get(iCol);
            String                  strColNm = itrColNms.next();
            
            Assert.assertEquals(strColNm, col.getName());
            Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
            Assert.assertEquals(enmType, col.getType());
            
            for (int iVal=0; iVal<szCol; iVal++) {
                Object  objVal = col.getValue(iVal);
                
                if (objVal instanceof Integer intVal) {
                    // Check value
                    Assert.assertEquals("Column " + strColNm + " value #" + iVal + " was bad.", intCurr, intVal);
                    lstVals.add(intVal);
                    
                    // Advance current test value
                    intCurr += intIncr;
                    
                } else
                    Assert.fail("Value of column " + strColNm + " value #" + iVal + " was not Integer.");
            }
            
            // Print out column values and clear value list
            System.out.println("  Column " + strColNm + " values: " + lstVals);
            lstVals.clear();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#build()}.
     */
    @Test
    public final void testBuildMultiIntegerIncr2() {
        
        // Test Parameters
        final Set<String>       setColNms = SET_COL_NMS_5;
        final int               szCol = 10;
        final ScalarFactoryEnum enmFac = ScalarFactoryEnum.INTEGER_INCR_2;
        
        final int               cntCols = setColNms.size();
        final ScalarFactory     facVals = enmFac.newFactory();
        final DpSupportedType   enmType = facVals.getDatumType();
        final int               intSeed = Math.toIntExact( enmFac.getConfiguration().seed() );
        final int               intIncr = facVals.getConfiguration().increment().intValue();
        
        final int               cntBlds = 3;

        // Create the column factory and check configuration
        DataColumnsFactory   facTest;
        try {
            facTest = DataColumnsFactory.from(setColNms, szCol, facVals);
            
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column names: " + setColNms);
        System.out.println("  Column count: " + cntCols);
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);

        // Create a column and check configuration and values
        Integer     intCurr = intSeed;
        
        for (int iBld=0; iBld<cntBlds; iBld++) {
            ArrayList<IDataColumn<Object>>  vecCols = facTest.build();
            Iterator<String>                itrColNms = setColNms.iterator();
            for (int iCol=0; iCol<cntCols; iCol++) {
                IDataColumn<Object>     col = vecCols.get(iCol);
                String                  strColNm = itrColNms.next();

                Assert.assertEquals(strColNm, col.getName());
                Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
                Assert.assertEquals(enmType, col.getType());

                for (int iVal=0; iVal<szCol; iVal++) {
                    Object  objVal = col.getValue(iVal);

                    if (objVal instanceof Integer intVal) {
                        // Check value
                        Assert.assertEquals("Column " + strColNm + " value #" + iVal + " was bad.", intCurr, intVal);

                        // Advance current test value
                        intCurr += intIncr;

                    } else
                        Assert.fail("Value of column " + strColNm + " value #" + iVal + " was not Integer.");
                }

            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.frames.DataColumnsFactory#build()}.
     */
    @Test
    public final void testBuildTensorMultiIntegerIncr1() {
        
        // Test Parameters
        final Set<String>       setColNms = SET_COL_NMS_5;
        final int               szCol = 10;
        final int[]             shape = { 1, 2, 3 };
        final TensorFactoryEnum enmFac = TensorFactoryEnum.INTEGER_INCR_1;
        
        final int               cntCols = setColNms.size();
        final TensorFactory     facVals = enmFac.newFactory(shape);
        final DpSupportedType   enmType = facVals.getDatumType();
        final JalScalarType     enmElemType = enmFac.getJalScalarType();
        final int               intSeed = Math.toIntExact( enmFac.getScalarFactoryConfig().seed() );
        final int               intIncr = enmFac.getScalarFactoryConfig().increment().intValue();
        
        final int               cntBlds = 3;

        // Create the column factory and check configuration
        DataColumnsFactory   facTest;
        try {
            facTest = DataColumnsFactory.from(setColNms, szCol, facVals);
            
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(szCol, facTest.getColumnSize());
            Assert.assertEquals(enmType, facTest.getColumnType());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsFactory creation/construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Print out test configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Column names: " + setColNms);
        System.out.println("  Column count: " + cntCols);
        System.out.println("  Column size : " + szCol);
        System.out.println("  Column type : " + enmType);

        // Create a column and check configuration and values
        Integer     intCurr = intSeed;
        
        for (int iBld=0; iBld<cntBlds; iBld++) {
            ArrayList<IDataColumn<Object>>  vecCols = facTest.build();
            Iterator<String>                itrColNms = setColNms.iterator();
            for (int iCol=0; iCol<cntCols; iCol++) {
                IDataColumn<Object>     col = vecCols.get(iCol);
                String                  strColNm = itrColNms.next();

                Assert.assertEquals(strColNm, col.getName());
                Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
                Assert.assertEquals(enmType, col.getType());

                for (int iVal=0; iVal<szCol; iVal++) {
                    Object  objVal = col.getValue(iVal);

                    if (objVal instanceof ArrayList vecTensor) {
                        Assert.assertTrue(TensorUtility.isElementValueOfType(enmElemType, vecTensor));
                        TensorIndexGenerator    genIndices = TensorIndexGenerator.from(shape);
                        
                        for (Integer[] index : genIndices) {
                            Object objElem = TensorUtility.extractElementAt(index, vecTensor);
                            
                            if (objElem instanceof Integer intVal) {
                                
                                // Check value
                                Assert.assertEquals("Column " + strColNm + " tensor #" + iVal + " element " + index + " was bad.", intCurr, intVal);
                                
                            } else
                                Assert.fail("Column " + strColNm + " tensor #" + iVal + " element " + index + " was not an Integer.");

                            // Advance current test value
                            intCurr += intIncr;
                        }

                    } else
                        Assert.fail("Value of column " + strColNm + " value #" + iVal + " was not Tensor.");
                }

            }
        }
    }

}
