/*
 * Project: dp-jal
 * File:	AppOptionsParser.java
 * Package: com.ospreydcs.dp.jal.tools.common.parse
 * Type: 	AppOptionsParser
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
 * @since Feb 12, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.parse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Parser for application command-line arguments string arrays.
 * </p>
 * <p>
 * This class is designed for parsing command-line arguments strings offered to Java applications by
 * the Java Virtual Machine (JVM).  The entry point for any Java application is the static main method with
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
 * This class is a specialization of base class <code>{@link AppArgumentsParser}</code>.  Here all application 
 * command-line options must be specified <em>a priori</em>, that is, at creation.  It is intended for arguments with
 * "nested options" where delimiter characters can appear within option values, in particular, within 
 * command-line variable values.
 * </p>
 * <p>
 * <h2>Variable Parsing</h2>
 * Consider the example where the command line for application <code>MyApp</code> is given as follows:
 * <pre>
 *   java MyApp --dcmp TRUE --frame '--label TestFrame --cols 1000 TestPv DOUBLE' --cnt 5 10 15 --output test/ingest/frame
 * </pre>
 * The <code>--frame</code> variable contains the nested value <code>'--label TestFrame --cols 1000 TestPv DOUBLE'</code>
 * which would not be parsed correctly by base class <code>{@link AppArgumentsParser}</code>.  This class will
 * parse the above exampled correctly, returning the variable value 
 * <code>'--label TestFrame --cols 1000 TestPv DOUBLE'</code> from {@link #parseVariable(String, String...)}</code>
 * and <code>{@link #parseVariable(String, int, String...)}</code>.
 * </p>
 * <p>
 * The proper treatment of nested command-line variable values comes at the cost of specifying all valid application
 * command-line options at creation. 
 * </p>
 * <p>
 * <h2>Other Options Parsing</h2>
 * Must all other command-line options parsing is identical to that of the base class.
 * See the class documentation for <code>{@link AppArgumentsParser}</code> for further details.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Objects of <code>{@link AppOptionsParser}</code> must be instantiated from creators 
 * <code>{@link #from(Collection)}</code> and <code>{@link #from(Collection, Collection)}</code>.
 * The zero-argument of creator <code>{@link #from()}</code> base class is not viable.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Feb 12, 2026
 *
 * @see AppArgumentsParser
 */
public class AppOptionsParser extends AppArgumentsParser {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>AppOptionsParser</code> instance for the given command-line options and
     * the default set of delimiters.
     * </p>
     * <p>
     * The returned <code>AppOptionsParser</code> instance is initialized with the given collection of delimited
     * command-line options.  The options include delimited switches, variable names, and property names.
     * The predefined command-line options in <code>{@link AppArgumentsParser#getPredefinedOptions()}</code> will 
     * also be appended to the given collection of delimited command-line options.
     * All other command-line options will be ignored.
     * </p> 
     * <p>
     * The returned <code>AppOptionsParser</code> instance is initialized with the default delimiters contained
     * in base class constant <code>{@link AppArgumentsParser#getPredefinedOptions()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Also adds all predefined options in <code>{@link AppArgumentsParser#SET_OPTS_PREDEF}</code>
     *     </li>into the collection of valid, delimited command-line options.
     * <li>Additional command-line options can be added with methods
     *      <code>{@link #addDelimitedOption(String)}</code> and <code>{@link #addDelimitedOptions(Collection)}</code>.</li>
     *      </li>
     * <li>Additional delimiter tokens can be added with methods
     *      <code>{@link #addDelimiter(String)}</code> and <code>{@link #addDelimiters(Collection)}</code>.
     *      </li>
     * <li>Command-line options are unique and any repeated values are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param conDelOptions the full collection of valid, delimited command-line options
     * 
     * @return  a new, initialized <code>AppOptionsParser</code> instance ready for parsing
     */
    public static AppOptionsParser  from(Collection<String> conDelOptions) {
        Set<String> setDelsDef = AppArgumentsParser.getDefaultDelimiters();
        
        return new AppOptionsParser(conDelOptions, setDelsDef);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>AppOptionsParser</code> instance for the given command-line options and
     * the given collection of custom delimiters.
     * </p>
     * <p>
     * The returned <code>AppOptionsParser</code> instance is initialized with the given collection of delimited
     * command-line options.  The options include delimited switches, variable names, and property names.
     * The predefined command-line options in <code>{@link AppArgumentsParser#SET_DVARS_PREDEF}</code> will 
     * also be appended to the given collection of delimited command-line options.
     * All other command-line options will be ignored.
     * </p> 
     * <p>
     * The returned <code>AppOptionsParser</code> instance is initialized with the given collection of
     * custom delimiter characters and string.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Also adds all predefined options in <code>{@link AppArgumentsParser#SET_OPTS_PREDEF}</code>
     *     </li>into the collection of valid, delimited command-line options.
     * <li>Additional command-line options can be added with methods
     *      <code>{@link #addDelimitedOption(String)}</code> and <code>{@link #addDelimitedOptions(Collection)}</code>.</li>
     *      </li>
     * <li>Additional delimiter tokens can be added with methods
     *      <code>{@link #addDelimiter(String)}</code> and <code>{@link #addDelimiters(Collection)}</code>.
     *      </li>
     * <li>Command-line options are unique and any repeated values are ignored.</li>
     * <li>Delimiter characters and strings are unique and any repeated values are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param conDelOptions the full collection of valid, delimited command-line options
     * @param conDelimiters the collection of custom delimiter characters and strings
     * 
     * @return  a new, initialized <code>AppOptionsParser</code> instance ready for parsing
     */
    public static AppOptionsParser  from(Collection<String> conDelOptions, Collection<String> conDelimiters) {
        return new AppOptionsParser(conDelOptions, conDelimiters);
    }
    
    
    //
    // Instance Resources
    //
    
    /** The set of valid command-line options for the application */
    private final Set<String>       setOptions = new TreeSet<>();
  

    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>AppOptionsParser</code> instance initialized with the given arguments.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Also adds all predefined options in <code>{@link AppArgumentsParser#SET_OPTS_PREDEF}</code>
     * into the collection of valid, delimited command-line options.
     * </p>
     *
     * @param conDelOpts    the collection of parser delimiter characters and strings
     * @param conDelimiters the collection of parser valid, delimited command-line options
     */
    private AppOptionsParser(Collection<String> conDelOpts, Collection<String> conDelimiters) {
        super(conDelimiters);
        
        this.setOptions.addAll(conDelOpts);
        this.setOptions.addAll(AppArgumentsParser.SET_DVARS_PREDEF);
    }

    
    // 
    // Configuration
    //
    
    /**
     * <p>
     * Adds the given delimited option to the parser's collection of command-line options.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Command-line options are unique and any repeated values are ignored.</li>
     * </p>
     *  
     * @param strDelOption  delimited command-line option to add
     */
    public void addDelimitedOption(String strDelOption) {
        this.setOptions.add(strDelOption);
    }
    
    /**
     * <p>
     * Adds the given collection of delimited options to the parser's collection of command-line options.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Command-line options are unique and any repeated values are ignored.</li>
     * </p>
     * 
     * @param conDelOptions collection of delimited command-line options to add
     */
    public void addDelimitedOptions(Collection<String> conDelOptions) {
        this.setOptions.addAll(conDelOptions);
    }
    
    /**
     * <p>
     * Returns the current set of unique delimited options.
     * </p>
     * <p>
     * The returned collection is the internal set of command-line options used by the parser
     * and should not be modified externally.
     * Use <code>{@link #addDelimitedOption(String)}</code> and <code>{@link #addDelimitedOptions(Collection)}</code>
     * to add command-line options after creation.
     * </p> 
     * 
     * @return  the current set of unique, delimited command-line options used by the parser 
     */
    public Set<String>  getDelimitedOptions() {
        return this.setOptions;
    }
    
    
    //
    // AppArgumentsParser Overrides
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, int, java.lang.String[])
     */
    @Override
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
                boolean bolEnd = this.setOptions.stream().anyMatch(strOpt -> strToken.startsWith(strOpt));
                if (bolEnd) 
                    break;

                lstVars.add(strToken);
            }
            iOccur++;
        }

        return lstVars;
    }

    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser#parseVariable(java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> parseVariable(String strDelVar, String...args) {
        
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
              boolean bolEnd = this.setOptions.stream().anyMatch(strOpt -> strToken.startsWith(strOpt));
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
}
