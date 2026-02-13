/*
 * Project: dp-jal
 * File:	AppArgumentsParserTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.parse
 * Type: 	AppArgumentsParserTest
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
 * @since Jan 5, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.parse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>AppArgumentsParser</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 5, 2026
 *
 */
public class AppArgumentsParserTest {

    
    //
    // Class Constants
    //
    
    /** Argument collection for parsing tests */
    public static final String[]        ARGS_PARSE_1 = { "-default", "-class", "--tags", "tag1", "tag2", "tag3", "-Attrname1=value1", "--tags", "tag4", "tag5", "--" };
    
    /** Argument collection for parsing tests */
    public static final String[]        ARGS_PARSE_2 = { "--frame", "tagsdef", "tagscls", "-Attrname1=value1", "-Attrname2=value2", "-Attrname3=value3" };
    
    /** Argument collection for parsing tests */
    public static final String[]        ARGS_PARSE_3 = { "-mthread", "--arch", "m2", "--threads", "10", "20", "-Pcore=10", "localhost" };
    
    /** Argument collection for parsing tests */
    public static final String[]        ARGS_PARSE_4 = { "create", "~new", "@usr", "bob", "fred", "#bin", "23", "42", "$acc", "10000", "**caveat", "domestic", "^^Ttrunk=T1", "remotehost" };
    
    
    //
    // Class Resources
    //
    
    /** The set of delimiting tokens typically used in command-line parsing */ 
    public static final Set<String>         SET_DELS_DEF = Set.of("-", "--");
    
    /** A set of atypical delimiting tokens */
    public static final Set<String>         SET_DELS_TEST = Set.of("@", "#", "^^", "**", "$", "~" );
    
    /** Collections of switches within parsing string 1 */
    public static final Collection<String>  CON_SWITCH_1 = Set.of("-default", "-class");
    
    /** The first --tag variable values for parsing string 1 */
    public static final List<String>        LST_TAGS_IND0_1 = List.of("tag1", "tag2", "tag3");
    
    /** The second --tag variable values for parsing string 1 */
    public static final List<String>        LST_TAGS_IND1_1 = List.of("tag4", "tag5");
    
    /** All the --tag variable values for parsing string 1 */
    public static final List<String>        LST_TAGS_1 = List.of("tag1", "tag2", "tag3", "tag4", "tag5");
    
    /** The collection of -Attr property (name, value) pairs for parsing string 1 */
    public static final Map<String, String> MAP_ATTRS_1 = Map.of("name1", "value1");

    
    /** The --frame variable values for parsing string 2 */
    public static final List<String>        LST_FRAME_2 = List.of("tagsdef", "tagscls");
    
    /** The collection of -Attr property (name, value) pairs for parsing string 2 */
    public static final Map<String, String> MAP_ATTRS_2 = Map.of("name1", "value1", "name2", "value2", "name3", "value3");
    
    
    /** Collections of switches within parsing string 3 */
    public static final Collection<String>  CON_SWITCH_3 = Set.of("-mthread");
    
    /** The --arch variable values for parsing string 3 */
    public static final List<String>        LST_ARCH_3 = List.of("m2");
    
    /** The --threads variable values for parsing string 3 */
    public static final List<String>        LST_THRDS_3 = List.of("10", "20");
    
    /** The collection of -P property (name, value) pairs for parsing string 3 */
    public static final Map<String, String> MAP_P_3 = Map.of("core", "10");
    
    /** The target of parsing string 3 */
    public static final String              STR_TARGET_3 = "localhost";
    
    
    /** The commands for parsing string 4 */
    public static final List<String>        LST_CMDS_4 = List.of("create");
    
    /** Collections of switches within parsing string 4 */
    public static final Collection<String>  CON_SWITCH_4 = Set.of("~new");
    
    /** The @usr variable values for parsing string 4 */
    public static final List<String>        LST_USR_4 = List.of("bob", "fred");
    
    /** The #bin variable values for parsing string 4 */
    public static final List<String>        LST_BIN_4 = List.of("23", "42");
    
    /** The $acc variable values for parsing string 4 */
    public static final List<String>        LST_ACC_4 = List.of("10000");
    
    /** The **caveat variable values for parsing string 4 */
    public static final List<String>        LST_CAVEAT_4 = List.of("domestic");
    
    /** The collection of ^^T property (name, value) pairs for parsing string 4 */
    public static final Map<String, String> MAP_T_4 = Map.of("trunk", "T1");
    
    /** The target of parsing string 4 */
    public static final String              STR_TARGET_4 = "remotehost";

    
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
    
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#from()}.
//     */
//    @Test
//    public final void testFrom() {
//        
//        // Create new parser and check configuration
//        AppArgumentsParser parser = AppArgumentsParser.from();
//        
//        Assert.assertTrue(parser.getDelimiters().isEmpty());
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#from(java.util.Collection)}.
     */
    @Test
    public final void testFromCollectionOfString() {

        // Test Parameters
        final Set<String>   setDels = SET_DELS_DEF;
        
        
        // Create new parser and check configuration
        AppArgumentsParser parser = AppArgumentsParser.from(setDels);
        
        Assert.assertEquals(setDels, parser.getDelimiters());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#from()}.
     */
    @Test
    public final void testFromDefault() {

        // Test Parameters
        final Set<String>   setDels = AppArgumentsParser.SET_DELS_DEF;
        
        
        // Create new parser and check configuration
        AppArgumentsParser parser = AppArgumentsParser.from(setDels);
        
        Assert.assertEquals(setDels, parser.getDelimiters());
        
        // Print out default configuration (tests AppArgumentsParser.toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration");
        System.out.println(parser);
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#AppArgumentsParser()}.
//     */
//    @Test
//    public final void testAppArgumentsParser() {
//        
//        // Create new parser and check configuration
//        AppArgumentsParser parser = new AppArgumentsParser();
//        
//        Assert.assertTrue(parser.getDelimiters().isEmpty());
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#AppArgumentsParser(java.util.Collection)}.
//     */
//    @Test
//    public final void testAppArgumentsParserCollectionOfString() {
//
//        // Test Parameters
//        final Set<String>   setDels = SET_DELS_DEF;
//        
//        
//        // Create new parser and check configuration
//        AppArgumentsParser parser = new AppArgumentsParser(setDels);
//        
//        Assert.assertEquals(setDels, parser.getDelimiters());
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#addDelimiter(java.lang.String)}.
     */
    @Test
    public final void testAddDelimiter() {
        
        // Test Parameters 
        final Set<String>   setDelsAdd = SET_DELS_TEST;
        final Set<String>   setDelsAll = Stream.concat(setDelsAdd.stream(), AppArgumentsParser.getDefaultDelimiters().stream()).collect(Collectors.toSet());
        
        // Create uninitialized parser and add delimiters one-by-one
        AppArgumentsParser parser = AppArgumentsParser.from();
        
        setDelsAdd.forEach(del -> parser.addDelimiter(del));
        
        Assert.assertEquals(setDelsAll, parser.getDelimiters());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#addDelimiters(java.util.Collection)}.
     */
    @Test
    public final void testAddDelimiters() {

        // Test Parameters 
        final Set<String>   setDels1 = SET_DELS_DEF;
        final Set<String>   setDels2 = SET_DELS_TEST;
        
        final Set<String>   setDelsAll = Stream.concat(setDels1.stream(), setDels2.stream()).collect(Collectors.toSet());
        
        // Create uninitialized parser and add delimiters 
        AppArgumentsParser parser = AppArgumentsParser.from(setDels1);
        
//        parser.addDelimiters(setDels1);
        parser.addDelimiters(setDels2);
        
        Assert.assertEquals(setDelsAll, parser.getDelimiters());
        
        // Print out specialized configuration (tests AppArgumentsParser.toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Specialized Configuration");
        System.out.println(parser);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseCommands(java.lang.String[])}.
     */
    @Test
    public final void testParseCommands1() {
        
        // Test Parameters
        final String[]      arrArgs = ARGS_PARSE_1;
        
        final List<String>  lstCmdsExpect = List.of();
        
        // Create default parser and parse commands
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        List<String>    lstCmds = parser.parseCommands(arrArgs);
        
        Assert.assertEquals(lstCmdsExpect, lstCmds);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseCommands(java.lang.String[])}.
     */
    @Test
    public final void testParseCommands4() {
        
        // Test Parameters
        final Set<String>   setDels = SET_DELS_TEST;
        final String[]      arrArgs = ARGS_PARSE_4;
        
        final List<String>  lstCmdsExpect = LST_CMDS_4;
        
        // Create default parser and parse commands
        AppArgumentsParser  parser = AppArgumentsParser.from(setDels);
        
        List<String>    lstCmds = parser.parseCommands(arrArgs);
        
        Assert.assertEquals(lstCmdsExpect, lstCmds);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#hasSwitch(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseSwitch0() {
        
        // Test Parameters
        final String[]              arrArgs = ARGS_PARSE_1;
        final Collection<String>    conSwitches = Set.of("-badSwitch1", "badSwitch2", "badSwitch3");

        // Create default parser, parse each switch and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        for (String strSwitch : conSwitches) {
            boolean bolResult = parser.hasSwitch(strSwitch, arrArgs);
            
            Assert.assertFalse(bolResult);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#hasSwitch(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseSwitch1() {
        
        // Test Parameters
        final String[]              arrArgs = ARGS_PARSE_1;
        final Collection<String>    conSwitches = CON_SWITCH_1;

        // Create default parser, parse each switch and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        for (String strSwitch : conSwitches) {
            boolean bolResult = parser.hasSwitch(strSwitch, arrArgs);
            
            Assert.assertTrue(bolResult);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#hasSwitch(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseSwitch3() {
        
        // Test Parameters
        final String[]              arrArgs = ARGS_PARSE_3;
        final Collection<String>    conSwitches = CON_SWITCH_3;

        // Create default parser, parse each switch and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        for (String strSwitch : conSwitches) {
            boolean bolResult = parser.hasSwitch(strSwitch, arrArgs);
            
            Assert.assertTrue(bolResult);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#hasSwitch(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseSwitch4() {
        
        // Test Parameters
        final Set<String>           setDels = SET_DELS_TEST;
        final String[]              arrArgs = ARGS_PARSE_4;
        final Collection<String>    conSwitches = CON_SWITCH_4;

        // Create specialized parser, parse each switch and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from(setDels);
        
        for (String strSwitch : conSwitches) {
            boolean bolResult = parser.hasSwitch(strSwitch, arrArgs);
            
            Assert.assertTrue(bolResult);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, int, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable1Ind0() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_1;
        final String            strDelVar = "--tags";
        final int               indOccur = 0;
        final List<String>      lstValsExpect = LST_TAGS_IND0_1;
        
        // Create default parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from();

        List<String>    lstVals = parser.parseVariable(strDelVar, indOccur, arrArgs);
        
        Assert.assertEquals(lstValsExpect, lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, int, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable1Ind1() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_1;
        final String            strDelVar = "--tags";
        final int               indOccur = 1;
        final List<String>      lstValsExpect = LST_TAGS_IND1_1;
        
        // Create default parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from();

        List<String>    lstVals = parser.parseVariable(strDelVar, indOccur, arrArgs);
        
        Assert.assertEquals(lstValsExpect, lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable1All() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_1;
        final String            strDelVar = "--tags";
        final List<String>      lstValsExpect = LST_TAGS_1;
        
        // Create default parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from();

        List<String>    lstVals = parser.parseVariable(strDelVar, arrArgs);
        
        Assert.assertEquals(lstValsExpect, lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable2() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_2;
        final String            strDelVar = "--frame";
        final List<String>      lstValsExpect = LST_FRAME_2;
        
        // Create default parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from();

        List<String>    lstVals = parser.parseVariable(strDelVar, arrArgs);
        
        Assert.assertEquals(lstValsExpect, lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable3() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_3;
        final String            strDelVar1 = "--arch";
        final String            strDelVar2 = "--threads";
        final List<String>      lstValsExpect1 = LST_ARCH_3;
        final List<String>      lstValsExpect2 = LST_THRDS_3;
        
        // Create default parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from();

        List<String>    lstVals1 = parser.parseVariable(strDelVar1, arrArgs);
        List<String>    lstVals2 = parser.parseVariable(strDelVar2, arrArgs);
        
        Assert.assertEquals(lstValsExpect1, lstVals1);
        Assert.assertEquals(lstValsExpect2, lstVals2);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseVariable4() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_4;
        final String            strDelVar1 = "@usr";
        final String            strDelVar2 = "#bin";
        final String            strDelVar3 = "**caveat";
        final String            strDelVar4 = "$acc";
        final List<String>      lstValsExpect1 = LST_USR_4;
        final List<String>      lstValsExpect2 = LST_BIN_4;
        final List<String>      lstValsExpect3 = LST_CAVEAT_4;
        final List<String>      lstValsExpect4 = LST_ACC_4;
        
        // Create specialized parser, parse variable and confirm values
        AppArgumentsParser  parser = AppArgumentsParser.from(SET_DELS_TEST);

        List<String>    lstVals1 = parser.parseVariable(strDelVar1, arrArgs);
        List<String>    lstVals2 = parser.parseVariable(strDelVar2, arrArgs);
        List<String>    lstVals3 = parser.parseVariable(strDelVar3, arrArgs);
        List<String>    lstVals4 = parser.parseVariable(strDelVar4, arrArgs);
        
        Assert.assertEquals(lstValsExpect1, lstVals1);
        Assert.assertEquals(lstValsExpect2, lstVals2);
        Assert.assertEquals(lstValsExpect3, lstVals3);
        Assert.assertEquals(lstValsExpect4, lstVals4);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseProperty(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseProperty1() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_1;
        final String            strDelProp = "-Attr";
        
        final Map<String, String>   mapPropsExpect = MAP_ATTRS_1;
        
        // Create default parser, parse property and confirm (name, value) pairs)
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        try {
            Map<String, String> mapProps = parser.parseProperty(strDelProp, arrArgs);
            
            Assert.assertEquals(mapPropsExpect, mapProps);
            
        } catch (ConfigurationException e) {
            Assert.fail("Property " + strDelProp + " failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseProperty(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseProperty2() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_2;
        final String            strDelProp = "-Attr";
        
        final Map<String, String>   mapPropsExpect = MAP_ATTRS_2;
        
        // Create default parser, parse property and confirm (name, value) pairs)
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        try {
            Map<String, String> mapProps = parser.parseProperty(strDelProp, arrArgs);
            
            Assert.assertEquals(mapPropsExpect, mapProps);
            
        } catch (ConfigurationException e) {
            Assert.fail("Property " + strDelProp + " failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseProperty(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseProperty3() {
        
        // Test Parameters
        final String[]          arrArgs = ARGS_PARSE_3;
        final String            strDelProp = "-P";
        
        final Map<String, String>   mapPropsExpect = MAP_P_3;
        
        // Create default parser, parse property and confirm (name, value) pairs)
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        try {
            Map<String, String> mapProps = parser.parseProperty(strDelProp, arrArgs);
            
            Assert.assertEquals(mapPropsExpect, mapProps);
            
        } catch (ConfigurationException e) {
            Assert.fail("Property " + strDelProp + " failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseProperty(java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testParseProperty4() {
        
        // Test Parameters
        final Set<String>       setDels = SET_DELS_TEST;
        final String[]          arrArgs = ARGS_PARSE_4;
        final String            strDelProp = "^^T";
        
        final Map<String, String>   mapPropsExpect = MAP_T_4;
        
        // Create specialized parser, parse property and confirm (name, value) pairs)
        AppArgumentsParser  parser = AppArgumentsParser.from(setDels);
        
        try {
            Map<String, String> mapProps = parser.parseProperty(strDelProp, arrArgs);
            
            Assert.assertEquals(mapPropsExpect, mapProps);
            
        } catch (ConfigurationException e) {
            Assert.fail("Property " + strDelProp + " failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseTarget(java.lang.String[])}.
     */
    @Test
    public final void testParseTarget1() {

        // Test Parameters
        final String[]      arrArgs = ARGS_PARSE_1;
        final String        strTgtExpect = null;
        
        // Create default parser, parse target and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        String  strTgt = parser.parseTarget(arrArgs);
        
        Assert.assertEquals(strTgtExpect, strTgt);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseTarget(java.lang.String[])}.
     */
    @Test
    public final void testParseTarget3() {

        // Test Parameters
        final String[]      arrArgs = ARGS_PARSE_3;
        final String        strTgtExpect = STR_TARGET_3;
        
        // Create default parser, parse target and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from();
        
        String  strTgt = parser.parseTarget(arrArgs);
        
        Assert.assertEquals(strTgtExpect, strTgt);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseTarget(java.lang.String[])}.
     */
    @Test
    public final void testParseTarget4() {

        // Test Parameters
        final Set<String>   setDels = SET_DELS_TEST;
        final String[]      arrArgs = ARGS_PARSE_4;
        final String        strTgtExpect = STR_TARGET_4;
        
        // Create default parser, parse target and confirm
        AppArgumentsParser  parser = AppArgumentsParser.from(setDels);
        
        String  strTgt = parser.parseTarget(arrArgs);
        
        Assert.assertEquals(strTgtExpect, strTgt);
    }

    /**
     * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Test Parameters
        Set<String>     setDels1 = SET_DELS_DEF;
        Set<String>     setDels2 = SET_DELS_TEST;
        Set<String>     setDelsDef = AppArgumentsParser.getDefaultDelimiters();
        Set<String>     setDelsTot = Stream.concat(setDels1.stream(), setDels2.stream()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        
        // Create default parser and parsers with various configurations and test equivalence
        AppArgumentsParser  parserDef = AppArgumentsParser.from();
        
        AppArgumentsParser  parser1 = AppArgumentsParser.from(setDels1);
        AppArgumentsParser  parser2 = AppArgumentsParser.from(setDels2);
        AppArgumentsParser  parserTot = AppArgumentsParser.from(setDelsTot);
        parserTot.addDelimiters(setDelsDef);
        
        Assert.assertFalse(parserDef.equals(parser1));
        Assert.assertFalse(parserDef.equals(parser2));
        Assert.assertFalse(parserDef.equals(parserTot));
        
        // Modify default parser and test equivalences
        parserDef.addDelimiters(setDels1);

        Assert.assertFalse(parserDef.equals(parser1));
        Assert.assertFalse(parserDef.equals(parser2));
        Assert.assertFalse(parserDef.equals(parserTot));
        
        // Modify default parser again and test equivalences
        parserDef.addDelimiters(setDels1);
        parserDef.addDelimiters(setDels2);

        Assert.assertFalse(parserDef.equals(parser1));
        Assert.assertFalse(parserDef.equals(parser2));
        Assert.assertTrue(parserDef.equals(parserTot));
        
    }


}
