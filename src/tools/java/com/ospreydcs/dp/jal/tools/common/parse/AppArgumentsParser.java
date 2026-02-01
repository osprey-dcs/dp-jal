/*
 * Project: dp-jal
 * File:	AppArgumentsParser.java
 * Package: com.ospreydcs.dp.jal.tools.common.parse
 * Type: 	AppArgumentsParser
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Parser for application command-line arguments string array.
 * </p>
 * <p>
 * This parser is specifically designed for parsing of command-line strings offered to Java applications by
 * the Java Virtual Machine (VM).  The entry point for any Java application is the static main method with
 * signature
 * <pre> 
 * <code>
 * public static main(String...args) {
 * ...
 * }
 * </code>
 * </pre>
 * This class is intended for the processing of the <code>args</code> argument of the above entry method, specifically,
 * the extraction of commands, switches, variables, and properties from the argument collection <code>args</code>.
 * </p>
 * <p>
 * <h2>Delimiters</h2>
 * Any <code>AppArgumentsParser</code> instance must be configured to recognize one or more delimiter characters or
 * tokens to properly parse command lines.  Delimiters are typically tokens such as <code>"-"</code> and <code>"--"</code>.
 * They are used to identify switches, variables, and properties within the command line.   
 * Delimiters can be specified either at creation/construction or with
 * methods <code>{@link #addDelimiter(String)}</code> and <code>{@link #addDelimiters(Collection)}</code>.
 * </p>
 * <p>
 * Once the <code>AppArgumentsParse</code> instance is configured with a collection of delimiters all switches, variables,
 * and properties must use one of these values to prefix their name identifier.  Typically switches are prefixed
 * with <code>"-"</code>, variables are prefixed with <code>"--"</code> and properties are prefixed with <code>"-"</code>.
 * <p>
 * <h2>Commands</h2>
 * Commands are instructions to the application that must occur directly after the application
 * invocation.  They have no delimiters unlike application command-line switches or variables (e.g., -o console).
 * Multiple commands can be included in the argument list, separated by white space.
 * See method documentation for <code>{@link #parseCommands(String[])}</code> for more information.
 * </p>
 * <p>
 * <h2>Switches</h2>
 * Command line "switches" are arguments that appear without parameters, typically instructing the
 * application to conform to a given configuration or to perform a specific action.  They are generally
 * marked with delimiters '-' or '--'.
 * Multiple switches can be included in the command line, all separated by white space.
 * See method documentation for <code>{@link #parseSwitch(String, String...)}</code> for more information.  
 * </p>
 * <p>
 * <h2>Variables</h2>
 * Variables are identified by a given white-space separated delimiter on the command line, for example 
 * <code>"--threads"</code>.  Any tokens following the variable are considered values, for example,
 * with statement <code>--threads 10 20</code> the tokens '10' and '20' are the values of variable <code>"--thread"</code>.
 * A variable may contain multiple values and a variable declaration may occur multiple times on the command line.  
 * All variable values must be separated by white space.
 * See method documentation for <code>{@link #parseVariable(String, String...)}</code> for more information. 
 * </p>
 * <p>
 * <h2>Properties</h2>
 * Property assignments within the application command line appear a (name, value) pairs separated by the
 * assignment separator token {@value #STR_PROP_ASSGN_SEP}.  
 * Their property type is identified with a zero white space
 * delimiter given by the argument.  For example, the Java command line allows system properties to be assigned
 * at Virtual Machine (VM) startup with the <code>-D</code> option.  This class does support Java VM properties
 * and, thus, a delimiter other than <code>-D</code> must be used for command-line properties.
 * See method documentation for <code>{@link #parseProperty(String, String...)}</code> for more information.
 * </p> 
 *
 * 
 * @author Christopher K. Allen
 * @since Jan 5, 2026
 *
 */
public class AppArgumentsParser {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new, uninitialized <code>AppArgumentsParser</code> instance.
     * </p>
     * <p>
     * Use the methods <code>{@link #addDelimiter(String)}</code> and <code>{@link #addDelimiters(Collection)}</code>
     * to supply the returned parser with delimiter tokens before parsing operations.
     * </p>
     * 
     * @return  a new, uninitialized <code>AppArgumentsParser</code> instance ready for parsing
     */
    public static final AppArgumentsParser  from() {
        return new AppArgumentsParser();
    }
    
    /**
     * <p>
     * Creates and returns a new <code>AppArgumentsParser</code> instance initialized with the given delimiters.
     * </p>
     * <p>
     * The collection of delimiter tokens is used for all parsing operations.  
     * Note that additional delimiter tokens can be added to the returned parser with methods
     * <code>{@link #addDelimiter(String)}</code> and <code>{@link #addDelimiters(Collection)}</code>.
     * </p>
     * 
     * @param conDelimiters special characters and tokens used to delimit all switches, variables, and properties
     * 
     * @return  a new <code>AppArgumentsParser</code> instance configured and ready to parse
     */
    public static final AppArgumentsParser  from(Collection<String> conDelimiters) {
        return new AppArgumentsParser(conDelimiters);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>AppArgumentsParser</code> instance configured with default delimiter tokens.
     * </p>
     * <p>
     * The returned <code>AppArgumentsParser</code> instance is initialized with the default delimiters
     * in class constant <code>{@link #SET_DELS_DEF}</code>.
     * </p>
     * 
     * @return  a new, initialized <code>AppArgumentsParser</code> instance ready for parsing
     */
    public static final AppArgumentsParser  fromDefault() {
        return AppArgumentsParser.from(SET_DELS_DEF);
    }
    
    
    //
    // Class Constants
    //
    
    /** The assignment separator between a property name and its value */
    public static final String          STR_PROP_ASSGN_SEP = "=";
    
    /** The set of delimiting tokens typically used in command-line parsing */ 
    public static final Set<String>     SET_DELS_DEF = Set.of("-", "--", "|");
    
    
    //
    // Special Arguments and Values
    //
    
    /** Special application argument switch for help - see {@link #parseHelpRequest(String[])} */
    public static final String      STR_SWTCH_HELP = "-h";

    /** Special application argument variable for help  - see {@link #parseHelpRequest(String[])} */
    public static final String      STR_VAR_HELP = "--help";
    
    
    /** Special application argument variable for version - see {@link #parseAppArgsVersion(String[])} */
    public static final String      STR_SWTCH_VERSION = "-v";

    /** Special application argument variable for version - see {@link #parseAppArgsVersion(String[])} */
    public static final String      STR_VAR_VERSION = "--version";

    
    /** Argument variable identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** Special application argument variable value for console output - see {@link #openOutputStream(String)} */
    public static final String      STR_ARG_VAL_STDOUT = "console";

    
    //
    // Resources
    //
    
    /** The set of delimiting characters and tokens used to identify switches, variables, and properties */
    private final Set<String>       setDels = new TreeSet<>();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>AppArgumentsParser</code> instance with empty delimiter collection.
     * </p>
     *
     */
    public AppArgumentsParser() {
    }
    
    /**
     * <p>
     * Constructs a new <code>AppArgumentsParser</code> instance initialized with the given collection of delimiters.
     * </p>
     *
     * @param conDelimiters special characters and tokens used to delimit all switches, variables, and properties
     */
    public AppArgumentsParser(Collection<String> conDelimiters) {
        this.setDels.addAll(conDelimiters);
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Adds the given delimiter token to the current set of delimiters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Duplicate delimiter tokens will be ignored.</li>
     * </ul>
     * </p>
     * 
     * @param strDelimiter  delimiter character or string to be added to current delimiter set
     */
    public void addDelimiter(String strDelimiter) {
        this.setDels.add(strDelimiter);
    }
    
    /**
     * <p>
     * Adds the given collection of delimiter tokens to the current set of delimiters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Duplicate delimiter tokens will be ignored.</li>
     * </ul>
     * </p>
     * 
     * @param conDelimiters collection of delimiter characters or strings to be added to the current delimiter set
     */
    public void addDelimiters(Collection<String> conDelimiters) {
        this.setDels.addAll(conDelimiters);
    }
    
    /**
     * <p>
     * Returns the current set of delimiting tokens for parsing of switches, variables, and properties.
     * </p>
     * 
     * @return  the collection of unique delimiter tokens used for command-line argument parsing
     */
    public Set<String>  getDelimiters() {
        return this.setDels;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Parses the application argument collection for a help request.
     * </p>
     * <p>
     * A value <code>true</code> is returned if any element in the argument collection is equal to the value 
     * {@value #STR_SWTCH_HELP} or {@value #STR_VAR_HELP}, where case is ignored.  
     * Otherwise a value <code>false</code> is returned.
     * </p>
     * <p>
     * This method is equivalent to <code>{@link #parseSwitch(String, String[])}</code> with the <code>String</code>
     * argument value as {@value #STR_SWTCH_HELP} or {@value #STR_VAR_HELP}.
     * </p>
     * 
     * @param args  the application argument collection
     *  
     * @return  <code>true</code> if the argument collection contained the elements {@link #STR_SWTCH_HELP} and/or {@value #STR_VAR_HELP} (case ignored),
     *          <code>false</code> otherwise
     */
    public boolean parseHelpRequest(String[] args) {
        
        // Check argument
        if (args==null)
            return false;
        
        // Look for help request
        boolean bolHelp = this.parseSwitch(STR_SWTCH_HELP, args) 
                       || this.parseSwitch(STR_VAR_HELP, args);
        
        return bolHelp;
    }
    
    /**
     * <p>
     * Parses the application argument collection for a version request.
     * </p>
     * <p>
     * A value <code>true</code> is returned if any element in the argument collection is equal to the value 
     * {@value #STR_SWTCH_VERSION} and/or {@value #STR_VAR_VERSION}, where case is ignored.  
     * Otherwise a value <code>false</code> is returned.
     * </p>
     * <p>
     * This method is equivalent to <code>{@link #parseSwitch(String, String[])}</code> with the <code>String</code>
     * argument value as {@value #STR_SWTCH_VERSION} and/or  {@value #STR_VAR_VERSION}.
     * </p>
     * 
     * @param args  the application argument collection
     *  
     * @return  <code>true</code> if the argument collection contained the element {@value #STR_SWTCH_VERSION} and/or {@value #STR_VAR_VERSION} (case ignored),
     *          <code>false</code> otherwise
     */
    public boolean    parseVersionRequest(String[] args) {
        
        // Check argument
        if (args==null)
            return false;
        
        // Look for version request
        boolean bolHelp = this.parseSwitch(STR_SWTCH_VERSION, args)  
                       || this.parseSwitch(STR_VAR_VERSION, args);
        
        return bolHelp;
    }
    
    /**
     * <p>
     * Parses the collection of application arguments for errors or a help request.
     * </p>
     * <p>
     * The list of arguments to the application is parsed for common errors.  Exceptions are also thrown
     * if a help or version request is encountered.
     * The following conditions are checked in order:
     * <ol>
     * <li>Wrong number of arguments, must be >= the specified number <code>IllegalArgumentException</code>)</li>
     * <li>A {@value #STR_VAR_HELP} appeared in the argument list (<code>IllegalCallerException</code>).</li>
     * <li>A {@value #STR_VAR_VERSION} appeared in the argument list (<code>IllegalCallerException</code>).</li>
     * <li>An argument did not start with a valid switch/variable identified in argument (<code>UnsupportedOperationException</code>).</li>
     * </ol>
     * </p>
     * 
     * @param cntMinArgs    the minimum number of required arguments to the application
     * @param lstDelOpts    list of valid delimited options (switches, variables, and properties with their delimiters - e.g., '-o', '--copy', etc.) 
     * @param args          the application argument list 
     * 
     * @throws IllegalArgumentException         wrong number of arguments (did not contain enough commands/options)
     * @throws IllegalCallerException           the client request application help or version message
     * @throws UnsupportedOperationException    an application argument contained an invalid option flag
     */
    public void parseOptionErrors(int cntMinArgs, List<String> lstDelOpts, String[] args) 
            throws IllegalArgumentException, IllegalCallerException, UnsupportedOperationException {

        // Check the argument count
        if (args==null || args.length < cntMinArgs)
            throw new IllegalArgumentException("The argument list " + args + " has lenth less than minimum " + cntMinArgs);
        
        // Check for help request
        boolean bolHelp = this.parseHelpRequest(args);
        if (bolHelp)
            throw new IllegalCallerException("The client requested help message.");
        
        // Check for version request
        boolean bolVersion = this.parseVersionRequest(args);
        if (bolVersion)
            throw new IllegalCallerException("The client requested version information.");
        
        // Check each argument for valid flag
        for (String strToken : args) {
            String strArg = strToken.strip();
            
            // Check if argument is delimited - if not move to the next one
            boolean bolDelimited = this.setDels.stream().anyMatch(s -> strArg.contains(s));
            if (!bolDelimited)
                continue;
            
            // The argument contains a delimiter indicating an option - check against list of valid options
            boolean bolValidArg = lstDelOpts.stream().anyMatch(strOpt -> strArg.startsWith(strOpt));
            if (!bolValidArg)
                throw new UnsupportedOperationException("Argument " + strArg + " is invalid; is not contained in valid option list " + lstDelOpts);
        }
        
        // If we are here all arguments have a valid flag and there was no help request
        return;
    }
    
    /**
     * <p>
     * Parses the application argument collection for application commands.
     * </p>
     * <p>
     * Commands are instructions to the application that must occur directly after the application
     * invocation.  They have no delimiters unlike application command-line switches or variables (e.g., -o console).
     * Multiple commands can be included in the argument list, separated by white space.
     * For example, for an application <code>'MyApp'</code> the commands are given as follows:
     * <pre>
     * <code>
     *   %java MyApp command1 command2 ... commandN --threads 42 --pivot 100
     * </code>
     * </pre>
     * where <code>command1 ... commandN</code> are the commands.
     * The tokens <code>--threads</code> and <code>--pivot</code> are command-line variables specific to the above example;
     * here they signify the end of the command values.  The delimiter token <code>'--'</code> must be part of the
     * current collection of delimiter tokens.
     * </p>
     * <p>
     * This method returns all application arguments recognized as commands in the order in which they appear on the 
     * command line. 
     * </p>
     * 
     * @param args  application argument collection
     * 
     * @return  ordered list of application commands
     */
    public List<String>   parseCommands(String[] args) {
        
        // Check arguments
        if (args==null || args.length<1)
            return List.of();

        // The returned list of commands
        List<String>    lstCmds = new LinkedList<>();
        
        for (String strToken : args) {
            String  strArg = strToken.strip();
            
            // Look for delimiters - if found we are done with commands
            boolean bolDelimited = this.setDels.stream().anyMatch(strDel -> strArg.startsWith(strDel)); 
            if (bolDelimited)
                break;
            
            lstCmds.add(strArg);
        }
        
        return lstCmds;
    }

    /**
     * <p>
     * Parses the given argument collection for the appearance of the given command switch.
     * </p>
     * <p>
     * <h2>Switches</h2>
     * Command line "switches" are arguments that appear without parameters, typically instructing the
     * application to conform to a given configuration or to perform a specific action.  They are generally
     * marked with delimiters '-' or '--'.
     * This method determines whether or not the given switch is present within the given application argument
     * collection, returning <code>true</code> if found and <code>false</code> if not present.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Multiple switches can be included in the command line, all separated by white space.  (However, this
     * method only selects for a single switch element.)  
     * For example, in the Unix command
     * <pre>
     * <code>
     *   %ls -l -a
     * </code>
     * </pre>
     * the arguments '-l' and '-a' are switches to the Unix command 'ls' instructing it to include given outputs
     * in a line-by-line format.
     * </p>
     * 
     * @param strSwitch     the command line switch searched for
     * @param args          the application argument collection
     * 
     * @return  <code>true</code> if the given switch was found in the argument collection, <code>false</code> otherwise
     */
    public boolean    parseSwitch(String strSwitch, String...args) {
        
        // Check argument
        if (args==null)
            return false;
        
        // Looks for the switch within argument collection
        boolean bolSwitch = Arrays.asList(args)
                .stream()
                .<String>map(arg -> arg.strip())
                .anyMatch(s -> s.contentEquals(strSwitch));
        
        return bolSwitch;
    }
    
    /**
     * <p>
     * Parses the application argument collection for the number of appearances of the given delimited variable name.
     * </p>
     * <p>
     * Typically a variable name appears only once on a command line (i.e., followed by its variable values).
     * However, there are circumstances when multiple occurrence of the same variable have context.
     * This method returns the number of occurrences of the given delimited variable name on the given command line.
     * </p>
     * <p>
     * <h2>Example</h2> 
     * For example, consider the case where an ingestion frame is being described.  
     * Then the command-line might contain a sequence of column descriptors of the following
     * form:
     * <pre>
     * <code>
     * java MyApp --tms 1000 PT0.001S --cols 1 PVStruct: STRUCTURE 3 2 --cols 100 PVDbl: SCALAR DOUBLE  
     * </code>
     * </pre>
     * where the variable <code>--tms</code> delimits the ingestion frame timestamps properties and
     * the variable <code>--cols</code> delimits the ingestion frame data column properties.
     * Note that the collection of data columns is being described by column data type; the above
     * example has 1 column of data structures and 100 columns of scalar double values.
     * In the above example this method would return 1 for the variable <code>--tms</code> and 
     * 2 for the variable <code>--cols</code>.
     * </p>       
     *   
     * @param strDelVar the command-line variable with delimiter
     * @param args      application command-line argument collection
     * 
     * @return  the number of occurrences of the given variable in the command line 
     */
    public int  parseVariableCount(String strDelVar, String...args) {
        
        // Check arguments
        if (args==null || args.length<1)
            return 0;

        // Count the number of variable occurrences
        int cntOccur = Arrays.asList(args)
                .stream()
                .<String>map(arg -> arg.strip())
                .filter(s -> s.startsWith(strDelVar))
                .mapToInt(s -> 1)
                .sum();
        
        return cntOccur;
    }
    
    /**
     * <p>
     * Parses the application argument collection for variable values at the given index of given delimited variable name.
     * </p>
     * <p>
     * Variables are identified by a given white-space separated delimiter on the command line.  Any tokens
     * following the variable are considered values.  A variable may contain multiple values and a variable
     * declaration may occur multiple times on the command line.  All variable values must be separated by
     * white space.  This method selects for the variable values at the given occurrence of the variable
     * value within the command line.
     * </p>
     * <p>
     * For example, the following is a valid command-line structure for a application name <code>MyApp</code>:
     * <pre>
     * <code>
     *   %java MyApp --threads 5 10 15 --pivot 10 20 --threads 20
     * </pre>
     * </code>
     * where the tokens <code>'--threads'</code> and <code>'--pivot'</code> are variables with values {5, 10, 15, 20}
     * and {10, 20}, respectively.  Note that the variables here have numeric values but are returned in their original 
     * string format.  Unlike method <code>{@link #parseVariable(String, String...)}</code>, this method is capable of 
     * selecting for the separate value occurrences of variable <code>--threads</code> (i.e., {5, 10, 15} and {20}).
     * </p> 
     * <p>
     * <h2>Example</h2> 
     * For example, consider the case where an ingestion frame is being described.  
     * Then the command-line might contain a sequence of column descriptors of the following
     * form:
     * <pre>
     * <code>
     * java MyApp --tms 1000 PT0.001S --cols 1 PVStruct: STRUCTURE 3 2 --cols 100 PVDbl: SCALAR DOUBLE  
     * </code>
     * </pre>
     * where the variable <code>--tms</code> delimits the ingestion frame timestamps properties and
     * the variable <code>--cols</code> delimits the ingestion frame data column properties.
     * Note that the collection of data columns is being described by column data type; the above
     * example has 1 column of data structures and 100 columns of scalar double values.
     * This type of format is useful when describing data of the same type but with different parameters
     * within the command line.
     * </p>
     * <p>
     * With the variable <code>--cols</code> in the above example, this method would return the following:
     * <ul>
     * <li>index = 0 &rarr; <code>{ 1 PVStruct: STRUCTURE 3 2}</code>.</li>
     * <li>index = 1 &rarr; <code>{ 100 PVDbl: SCALAR DOUBLE}</code>.</li>
     * </ul>
     * </p>       
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If the variable name identified by the first argument is not present in the command-line arguments an empty
     * list is returned.
     * </li>
     * <li>
     * If the variable name is present at the given index but no values are given an empty list is returned.
     * </li>
     * <li>
     * The variable name (with delimiter) can occur multiple times on the command line.  If so, the variable values
     * for the specified occurrence of the variable name will be contained in the returned list.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strDelVar the command-line variable with delimiter
     * @param indOccur  the index of the variable name occurrence within the command line
     * @param args      application command-line argument collection
     * 
     * @return  ordered list of variable values for the given variable occurrence taken from the command line arguments 
     * 
     * @throws IndexOutOfBoundsException    the given index is greater than or equal to the number of variable name occurrences
     */
    public List<String> parseVariable(String strDelVar, int indOccur, String...args) throws IndexOutOfBoundsException {
        
        // Check arguments
        int cntOccur = this.parseVariableCount(strDelVar, args);
        
        if (cntOccur == 0)
            return List.of();
        
        if (indOccur >= cntOccur)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedCallerName() 
                    + " - Index " + indOccur 
                    + " >= number " + cntOccur 
                    + " of variable " + strDelVar + " occurences.");
        
        // The returned variables list
        List<String>    lstVars = new LinkedList<>();
        
        int iOccur = 0;
        int iArg = 0;
        while (iArg < args.length && iOccur <= indOccur) {
            String  strArg = args[iArg].strip();

            // Keep parsing argument list until we find a variable delimiter
            if (!strArg.contentEquals(strDelVar)) {
                iArg++;
                continue;
            }
            
            // We have found variable delimiter - advance to variable values
            iArg++;
            
            // Keep parsing argument list until we find the correct variable occurrence
            if (iOccur < indOccur) {
                iOccur++;
                continue;
            }

            // Where are at the correct index of the variable 
            //  Keep reading variable values until next delimiter, or end of arguments
            for (int i=iArg; i<args.length; i++) {
                String  strToken = args[i].strip();

                // If we find a delimiter we are at the end of the variable list
                boolean bolEnd = this.setDels.stream().anyMatch(strDel -> strToken.startsWith(strDel));
                if (bolEnd) 
                    break;

                lstVars.add(strToken);
            }
            iOccur++;
        }
        
        return lstVars;
    }
    
    /**
     * <p>
     * Parses the application argument collection for all variable values with the given delimited variable name.
     * </p>
     * <p>
     * Variables are identified by a given white-space separated delimiter on the command line.  Any tokens
     * following the variable are considered values.  A variable may contain multiple values and a variable
     * declaration may occur multiple times on the command line.  All variable values must be separated by
     * white space.
     * </p>
     * <p>
     * For example, the following is a valid command-line structure for a application name <code>MyApp</code>:
     * <pre>
     * <code>
     *   %java MyApp --threads 5 10 15 --pivot 10 20 --threads 20
     * </pre>
     * </code>
     * where the tokens <code>'--threads'</code> and <code>'--pivot'</code> are variables with values {5, 10, 15, 20}
     * and {10, 20}, respectively.  Note that the variables here have numeric values but are returned in their original 
     * string format.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If the variable name identified by the first argument is not present in the command-line arguments an empty
     * list is returned.
     * </li>
     * <li>
     * If the variable name is present but no values are given an empty list is returned.
     * </li>
     * <li>
     * The variable name (with delimiter) can occur multiple times on the command line.  If so, the variable values
     * for all occurrences of the variable name will be included in the returned list.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strDelVar the command-line variable with delimiter
     * @param args      application command-line argument collection
     * 
     * @return  ordered list of variable values taken from the command line arguments (empty if variable name not present)
     */
    public List<String>   parseVariable(String strDelVar, String...args) {
        
//        // Check arguments
//        if (args==null || args.length<1)
//            return List.of();
//
//        // Count the number of variable occurrences
//        int cntOccur = Arrays.asList(args)
//                .stream()
//                .<String>map(arg -> arg.strip())
//                .filter(s -> s.startsWith(strDelVar))
//                .mapToInt(s -> 1)
//                .sum();
        
        // Check arguments
        int cntOccur = this.parseVariableCount(strDelVar, args);
        
        if (cntOccur == 0)
            return List.of();
        
        // The returned variables list
        List<String>    lstVars = new LinkedList<>();
        
        int iOccur = 0;
        int iArg = 0;
        while (iArg < args.length && iOccur < cntOccur) {
            String  strArg = args[iArg].strip();

            // Keep parsing argument list until we find a variable delimiter
            if (!strArg.contentEquals(strDelVar)) {
                iArg++;
                continue;
            }
            
            // We have found variable delimiter - advance to variable values
            iArg++;

            // Keep reading variable values until next delimiter, or end of arguments
            for (int i=iArg; i<args.length; i++) {
                String  strToken = args[i].strip();

                // If we find a delimiter we are at the end of the variable list
                boolean bolEnd = this.setDels.stream().anyMatch(strDel -> strToken.startsWith(strDel));
                if (bolEnd) {
                    iOccur++;
                    break;
                }

                lstVars.add(strToken);
                iArg++;
            }
        }
        
        return lstVars;
    }
    
    /**
     * <p>
     * Parses the application argument collection for property assignment pairs with the given delimiter.
     * </p>
     * <p>
     * <h2>Properties</h2>
     * Property assignments within the application command line appear a (name, value) pairs separated by the
     * assignment separator token {@value #STR_PROP_ASSGN_SEP}.  
     * Their property type is identified with a zero white space
     * delimiter given by the argument.  For example, the Java command line allows system properties to be assigned
     * at Virtual Machine (VM) startup with the <code>-D</code> option.  Thus, to include the system properties
     * <code>prop1, ..., propN</code> into the Java VM with property values <code>val1, ..., valN</code>, respectively,
     * one would use the following command
     * <pre>
     * <code>
     *   %java -Dprop1=val1 ... -DpropN=valN MyApp
     * </code>
     * </pre>
     * where <code>MyApp</code> is the name of the Java application being launched.
     * This method <b>does not</b> return system properties.
     * </p>
     * <p>
     * <h2>Correct Usage</h2>
     * To launch application <code>MyApp</code> supplying it with a property with delimiter '-P' and supplying the
     * Java VM with the system properties as before one would invoke the following:
     * <pre>
     * <code>
     *   %java -Dprop1=val1 ... -DpropN=valN MyApp -PmyProp=myVal
     * </code>
     * </pre>
     * where <code>'myProp'</code> is the property name and <code>'myVal'</code> is the property value.  Of course
     * any delimiter may be chosen, which has context within the application.  This method parses the applications
     * arguments after the <code>MyApp</code> token.  That is, system properties are <b>not</b> returned.
     * </p>
     * <p>
     * The method returns the (name, value) pairs as (key, value) entries within the returned map.  If no properties
     * with the given delimiter are found an empty map is returned.  If a property with a bad assignment format is
     * found an exception is thrown.
     * </p>
     * 
     * @param strDelProp    the command-line property delimiter (e.g., <code>"-P"</code>)
     * @param args          application command-line argument collection
     * 
     * @return  map of (name, value) properties for the given property delimiter
     * 
     * @throws ConfigurationException   the property assignment format was bad 
     */
    public Map<String, String>   parseProperty(String strDelProp, String...args) throws ConfigurationException {
        
        // Check arguments
        if (args==null || args.length<1)
            return Map.of();
        
        // The returned map of property (name, value) pairs
        Map<String, String>     mapProps = new HashMap<>();
        
        // Parse the arguments for property assignment delimiter
        for (String strArg : args) {
            strArg = strArg.strip();
            
            // Found one - process
            if (strArg.startsWith(strDelProp)) {
                String      strElem = strArg.substring(strDelProp.length()); // skip the delimiter
                String[]    arrTokens = strElem.split(STR_PROP_ASSGN_SEP);        // split at assignment
                
                // Check for correct format
                if (arrTokens.length != 2)
                    throw new ConfigurationException("Bad property assignment argument " + strArg + ", format is not valid.");
                
                // Extract the (name, value) pair and add to map
                String  strName = arrTokens[0];
                String  strValue = arrTokens[1];
                
                mapProps.put(strName, strValue);
            }
        }

        return mapProps;
    }
    
    /**
     * <p>
     * Parses the application argument collection for the "target" of the application.
     * </p>
     * <p>
     * Returns the last argument in the given application argument collection, or <code>null</code> if the
     * collection is <code>null</code>, has size 0, or the last argument is delimited.
     * </p> 
     * <p>
     * <h2>Application Target</h2>
     * The "target" of an application, if present, is generally the last argument in the argument collection.
     * Of course this operation only has meaning in the context of the application where the last argument is
     * neither a command or a variable value.  This method simply selects the last argument in the argument collection
     * and returns it, so long as it does not start with a delimiter in the current collection of delimiters. 
     * </p>
     * <p>
     * As an example, a Java Virtual Machine (VM) execution "target" is the name of the class containing the
     * <code>main(String[])</code> entry point for an application.
     * To launch application <code>MyApp</code> within a JAR file supplying the
     * Java VM with the system properties <code>prop1, ..., propN</code> one would invoke the following:
     * <pre>
     * <code>
     *   %java -Dprop1=val1 ... -DpropN=valN -cp MyJar.jar com.mycompany.mypackage.MyApp 
     * </code>
     * </pre>
     * where <code>MyJar.jar</code> is the JAR file containing the main class and <code>com.mycompany.mypackage.MyApp</code>
     * is the fully qualified name of the main class.
     * </p>
     * 
     * @param args          application argument collection
     * 
     * @return  the target (last) argument of the command line, or <code>null</code> if not present
     */
    public String    parseTarget(String[] args) {
        
        // Check arguments
        if (args==null || args.length<1)
            return null;
        
        // Get the last argument
        int     cntArgs = args.length;
        String  strArgLast = args[cntArgs - 1];
        
        // Check if delimiter exists
        boolean bolDelimited = this.setDels.stream().anyMatch(strDel -> strArgLast.startsWith(strDel));
        if (bolDelimited)
            return null;
        
        return strArgLast;
    }
    
    
    //
    // Object Overrides
    //
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean  equals(Object obj) {
        if (obj instanceof AppArgumentsParser parser) {
            boolean bolResult = this.setDels.containsAll(parser.setDels)
                              && parser.setDels.containsAll(this.setDels);
            
            return bolResult;
        }
        
        return false;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String   toString() {
        String  str = this.getClass().getSimpleName() + "\n";
        str += "  property assignment separator: " + AppArgumentsParser.STR_PROP_ASSGN_SEP + "\n";
        str += "  delimiter tokens             : " + this.setDels + "\n";
        
        return str;
    }
    
}
