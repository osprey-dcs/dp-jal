/*
 * Project: dp-jal
 * File:	AppInputFileParser.java
 * Package: com.ospreydcs.dp.jal.tools.common.parse
 * Type: 	AppInputFileParser
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
 * @since Feb 14, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Parser class for files containing the command-line arguments of a Java application.
 * </p>
 * <p>
 * For the situations where Java applications allow very large command-line argument sets it is
 * sometimes convenient to store them in text files.  Class <code>AppInputFileParser</code> is available
 * for conversion of these text files into Java string arrays (i.e., objects of type <code>String[]</code>)
 * that are compatible with the <code>main(String[])</code> method entry point for Java applications.
 * </p>
 * <p>
 * The entry point for any Java application is the static main method with
 * signature
 * <pre> 
 * <code>
 * public static main(String...args) {
 * ...
 * }
 * </code>
 * </pre>
 * This class is intended for the production of the <code>args</code> argument for above entry method from text
 * files containing those arguments.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Instances of <code>AppInputFileParser</code> are only available through the class creators <code>from(...)</code>.
 * The constructor <code>{@link #AppInputFileParser(String, String[])}</code> is protected and available only
 * to child classes.
 * </p>
 * <p>
 * Note that creator <code>{@link #from()}</code> yields an <code>AppInputFileParser</code> instance with
 * default configuration, where single quotes and double quotes are used for nested options (see below)
 * and comment lines are identified with {@value #STR_COMMENT_DEF}.  This configuration is suitable for
 * most situations. 
 * </p>
 * <p>
 * <h2>Input File Parsing</h2>
 * The parsing of files containing the command lines of Java applications is performed by methods
 * <code>{@link #parseFile(String)}</code> and <code>{@link #parseFile(Path)}</code>.  These are the 
 * primary methods for the class.
 * </p>
 * <p>
 * <h2>Input File Reading</h2>
 * Class <code>AppInputFileParser</code> also provides methods <code>{@link #readFile(String)}</code> and
 * <code>{@link #readFile(Path)}</code> as a convenience for reading input files and returning their 
 * contents as contiguous character strings.  No parsing of the file contents is performed.  The method
 * <code>{@link #parseText(String)}</code> is available for parsing the returned text into argument arrays
 * for Java <code>main(String[])</code> entry points.
 * </p>
 * <p>
 * <h2>Comment Lines</h2>
 * Class <code>AppInputFileParser</code> accommodates the inclusion of comment lines within the application 
 * command-line arguments file.  The default comment character is {@value #STR_COMMENT_DEF} held in class 
 * constant <code>{@link #STR_COMMENT_DEF}</code>.  Alternate comment characters (or character strings) can
 * be supplied at creation/construction.  Any line within the input file starting with a comment token will
 * be ignored in the parsing.
 * </p>
 * <p>
 * <h2>Nested Options</h2>
 * Sometimes command-line arguments contain "nested options."  This is where a command-line delimited option,
 * typically a variable, contains values that also have delimited options.  Typically the nested options are
 * identified with single-quotes or double-quotes to enclose their values.   
 * Class <code>AppInputFileParser</code> is able to treated these cases using regular expression parsing.
 * </p>
 * Consider the following example input file containing the input arguments for a Java application:
 * <pre>
 * # Use column serialization
 * --serial FALSE TRUE 
 * 
 * # Use ingestion frame decomposition
 * --dcmp FALSE TRUE  
 * --threads 2 3 4 
 * 
 * # Ingestion frame definition
 * --frame '--label FrmDef1: --cols 10 PvStruct: STRUCTURE 2 3 FALSE DOUBLE' 
 *         '--label FrmDef2: --cols 1000 PvDbl: SCALAR DOUBLE'
 *         
 * # Ingestion frame count for evaluation payload
 * --nfrms 1 2
 * </pre>  
 * The single quote character "'" is used to identify the nested values of variable <code>--frame</code>.
 * Instances of <code>AppInputFileParser</code> will correctly parse the above values into single
 * string tokens for the above case.
 * </p>
 * In the default configuration <code>AppInputFileParser</code> objects use 3 regular expressions for
 * parsing input files:
 * <ol>
 * <li><code>{@link #STR_REGEX_SNG_QUOTE}</code> = {@value #STR_REGEX_SNG_QUOTE} for single-quote nesting,</li>
 * <li><code>{@link #STR_REGEX_DBL_QUOTE}</code> = {@value #STR_REGEX_DBL_QUOTE} for double-quote nesting,</li>
 * <li><code>{@link #STR_REGEX_CHAR_SEQ}</code> = {@value #STR_REGEX_CHAR_SEQ} for normal character tokens.</li>
 * </ol>
 * With the various creators, clients can provide their own regular expressions for specialized token parsing
 * within input files.  Creators allow allow clients to use custom comment-line identifiers.
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * It is imperative that a nested option appears within one line for correct parsing.
 * </p> 
 *  
 *
 * @author Christopher K. Allen
 * @since Feb 14, 2026
 *
 */
public class AppInputFileParser {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>AppInputFileParser</code> with all default configuration.
     * </p>
     * <p>
     * This creator produces a <code>AppInputFileParser</code> instance using the default comment string
     * {@value #STR_COMMENT_DEF} in class constant <code>{@link #STR_COMMENT_DEF}</code>.  
     * The default regular expression strings in <code>{@link #ARR_REGEX_DEF}</code> are used for token 
     * parsing of all input file contents.
     * </p>
     * 
     * @param strComment    character or string identifying comment lines within input file
     * 
     * @return  a new <code>AppInputFileParser</code> ready for parsing file containing Java application command-line arguments
     */
    public static AppInputFileParser    from() {
        return AppInputFileParser.from(STR_COMMENT_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>AppInputFileParser</code> configured with the given argument(s).
     * </p>
     * <p>
     * This creator produces a <code>AppInputFileParser</code> instance using a custom comment string.  
     * The default regular expression strings in <code>{@link #ARR_REGEX_DEF}</code> are used for token 
     * parsing of all input file contents.
     * </p>
     * 
     * @param strComment    character or string identifying comment lines within input file
     * 
     * @return  a new <code>AppInputFileParser</code> ready for parsing file containing Java application command-line arguments
     */
    public static AppInputFileParser    from(String strComment) {
        return AppInputFileParser.from(strComment, ARR_REGEX_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>AppInputFileParser</code> configured with the given argument(s).
     * </p>
     * <p>
     * This creator produces a fully custom <code>AppInputFileParser</code> instance.  The regular expression strings
     * provided in the argument are used for token parsing of all input file contents.
     * </p>
     * 
     * @param strComment    character or string identifying comment lines within input file
     * @param arrRegex      custom regular expressions for parsing input file tokens
     * 
     * @return  a new <code>AppInputFileParser</code> ready for parsing file containing Java application command-line arguments
     */
    public static AppInputFileParser    from(String strComment, String...arrRegex) {
        return new AppInputFileParser(strComment, arrRegex);
    }
    
    
    //
    // Class Constants
    //
    
    /** Regular expression for parsing nested tokens using double quotes (includes escapes) */ 
    public static final String      STR_REGEX_DBL_QUOTE = "\"((?:\\\\.|[^\"\\\\])*)\""; 

    /** Regular expression for parsing nested tokens using single quotes (includes escapes) */ 
    public static final String      STR_REGEX_SNG_QUOTE = "|'((?:\\\\.|[^'\\\\])*)'";
    
    /** Regular expression for parsing common string tokens (i.e., character strings) */
    public static final String      STR_REGEX_CHAR_SEQ = "|(\\S+)";
    
    
    /** The default comment character for comment lines */
    public static final String      STR_COMMENT_DEF = "#";

    /** Default regular expression token patterns for parsing input file */
    public static final String[]    ARR_REGEX_DEF = { STR_REGEX_DBL_QUOTE, STR_REGEX_SNG_QUOTE, STR_REGEX_CHAR_SEQ };
    
    
    // 
    // Defining Attributes
    //
    
    /** The character or character string used to identify comment lines */
    protected final String      strComment;
    
    /** The array of regular expressions each selecting a targeted token format */
    protected final String[]    arrRegex;
    
    
    
    //
    // Instance Resources
    //
    
    /** The number of pattern groups within strRegex - this should be the number of element in arrRegex */
    protected final int         cntGroups;
    
    /** The regular expression string used for parsing command-lines with nested tokens - concatenation of all {@link #arrRegex} strings */
    protected final String      strRegex;
    
    /** The token pattern compiled from the regular expression given at construction */
    protected final Pattern     pattern;
    
    
    //
    // Constructor
    //

    /**
     * <p>
     * Constructs a new <code>AppInputFileParser</code> instance.
     * </p>
     *
     */
    protected AppInputFileParser(String strComment, String[] arrRegex) {
        this.strComment = strComment;
        this.arrRegex = arrRegex;
        
        this.cntGroups = this.arrRegex.length;
        this.strRegex = Arrays.asList(this.arrRegex).stream().reduce((p1, p2) -> p1.concat(p2)).get();  // throws NoSuchElementException
        this.pattern = Pattern.compile(this.strRegex);
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Reads all contents of the file given by the argument and returns as a single string.
     * </p>
     * <p>
     * No parsing is performed.  The contents of the given file are read and returned as a 
     * contiguous character string, include any escaped characters such as tabs, newlines, and spaces.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument must be a string representation of a valid file path or an exception is thrown.</li>
     * <li>The target file must contain text or bytes that can be converted to the UTF-8 character set.</li>
     * </ul>
     * <p>
     * 
     * @implNote
     * The method defers to <code>{@link #readFile(Path)}</code> after first converting the argument to a 
     * <code>{@link Path}</code> object with <code>{@link Path#of(String, String...)}</code>.
     * 
     * @param strFile   string representation of a file path
     * 
     * @return  the contents of the given file
     * 
     * @throws InvalidPathException the argument did not represent a valid file path
     * @throws SecurityException    Unable to access the given file 
     * @throws IOException          error opening, reading, or closing the given file
     * @throws OutOfMemoryError     if the file is too large to read into a single string (e.g., > 2GB)
     */
    public String   readFile(String strFile) throws InvalidPathException, SecurityException, IOException, OutOfMemoryError {
        Path    pathFile = Path.of(strFile);    // throws InvalidPathException
        
        return this.readFile(pathFile);        // throws SecurityException, IOException
    }
    
    /**
     * <p>
     * Reads all contents of the file given by the argument and returns as a single string.
     * </p>
     * <p>
     * No parsing is performed.  The contents of the given file are read and returned as a 
     * contiguous character string, include any escaped characters such as tabs, newlines, and spaces.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The target file must contain text or bytes that can be converted to the UTF-8 character set.</li>
     * </ul>
     * <p>
     * @implNote
     * The method defers to <code>{@link Files#readString(Path)}</code>. 
     * 
     * @param pathFile   string representation of a file path
     * 
     * @return  the contents of the given file
     * 
     * @throws SecurityException    Unable to access the given file 
     * @throws IOException          error opening, reading, or closing the given file
     * @throws OutOfMemoryError     if the file is too large to read into a single string (e.g., > 2GB)
     */
    public String   readFile(Path pathFile) throws SecurityException, IOException, OutOfMemoryError {
        String  strText = Files.readString(pathFile);   // throws SecurityException, IOException
        
        return strText;
    }
    
    /**
     * <p>
     * Parses the given contiguous text string and returns the corresponding Java <code>main(String[])</code> argument.
     * </p>
     * <p>
     * Treats the given text string as if it were an unparsed command-line arguments collection to a Java application.
     * The command-line tokens are parsed from the argument according to the regular expression provided at 
     * construction/creation.  The tokens are then packed, in order, into a Java <code>String[]</code> object
     * and returned.  The returned array is compatible with the Java <code>main(String[])</code> method application
     * entry point.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument must be a string representation of a valid file path or an exception is thrown.</li>
     * </ul>
     * <p>
     * 
     * @implNote
     * The method wraps the argument into a <code>{@link BufferedReader}</code> object using
     * <code>{@link StringReader(String)}</code> then
     * <code>{@link BufferedReader(StringReader)}</code>.  The buffered reader is then passed
     * to internal method <code>{@link #parseLines(BufferedReader)}</code> for token parsing. 
     *  
     * @param strText   contiguous text string containing application command-line
     * 
     * @return  the application command in proper string-token format
     * 
     * @throws IOException                  unable to read buffer contents
     * @throws IllegalStateException        no match operation (i.e., {@link Matcher#find()}) attempted for the given matcher
     * @throws IndexOutOfBoundsException    attempted to read a pattern with index > {@link #arrRegex} length.
     */
    public String[] parseText(String strText) throws IOException, IllegalStateException, IndexOutOfBoundsException {
        
        BufferedReader    buf = new BufferedReader(new StringReader(strText));
        
        return this.parseLines(buf);
    }
    
    /**
     * <p>
     * Parses all contains of the given file into a string array compatible with the command-line arguments of Java <code>main(String[])</code>.
     * </p>
     * <p>
     * Treats the given text file as if it were an unparsed command-line arguments collection to a Java application.
     * The command-line tokens are parsed from the file contents according to the regular expression provided at 
     * construction/creation.  The tokens are then packed, in order, into a Java <code>String[]</code> object
     * and returned.  The returned array is compatible with the Java <code>main(String[])</code> method application
     * entry point.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument must be a string representation of a valid file path or an exception is thrown.</li>
     * </ul>
     * <p>
     * 
     * @implNote
     * The method converts the argument to a <code>{@link Path}</code> object using 
     * <code>{@link Path#of(String, String...)}</code> then defers to method <code>{@link #parseFile(Path)}</code>.
     * 
     * @param strFile   string representation of file path containing application command-line arguments
     * 
     * @return  string array of parsed command-line tokens according to regular expression provided at creation/construction
     * 
     * @throws InvalidPathException the argument did not represent a valid file path
     * @throws SecurityException    Unable to access the given file 
     * @throws IOException          error opening, reading, or closing the given file
     * @throws IllegalStateException        no match operation (i.e., {@link Matcher#find()}) attempted for the given matcher
     * @throws IndexOutOfBoundsException    attempted to read a pattern with index > {@link #arrRegex} length.
     */
    public String[] parseFile(String strFile) throws InvalidPathException, SecurityException, IOException, IllegalStateException, IndexOutOfBoundsException {
        Path    pathFile = Path.of(strFile);    // throws InvalidPathException
        
        return this.parseFile(pathFile);        // throws IOException, SecurityException
    }
    
    /**
     * <p>
     * Parses all contains of the given file into a string array compatible with the command-line arguments of Java <code>main(String[])</code>.
     * </p>
     * <p>
     * Treats the given text file as if it were an unparsed command-line arguments collection to a Java application.
     * The command-line tokens are parsed from the file contents according to the regular expression provided at 
     * construction/creation.  The tokens are then packed, in order, into a Java <code>String[]</code> object
     * and returned.  The returned array is compatible with the Java <code>main(String[])</code> method application
     * entry point.
     * </p> 
     * 
     * @implNote
     * The method opens a file-based <code>{@link BufferedReader}</code> to the argument location using 
     * <code>{@link Files#newBufferedReader(Path)}</code>.  The <code>BufferedReader</code> is then passed
     * to internal method <code>{@link #parseLines(BufferedReader)}</code> for file contents parsing. 
     * 
     * @param pathFile  file path containing application command-line arguments
     * 
     * @return  string array of parsed command-line tokens according to regular expression provided at creation/construction
     * 
     * @throws SecurityException    Unable to access the given file 
     * @throws IOException          error opening, reading, or closing the given file
     * @throws IllegalStateException        no match operation (i.e., {@link Matcher#find()}) attempted for the given matcher
     * @throws IndexOutOfBoundsException    attempted to read a pattern with index > {@link #arrRegex} length.
     */
    public String[]    parseFile(Path pathFile) throws SecurityException, IOException, IllegalStateException, IndexOutOfBoundsException {
        BufferedReader  buf = Files.newBufferedReader(pathFile);    // throws IOException, SecurityException
        
        return this.parseLines(buf);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Parses the buffered text extracting tokens according to the regular expression provided at construction.
     * </p>
     * <p>
     * The argument is assumed to contain the input arguments of a Java application in standard text format.
     * The text is parsed line-by-line, ignoring comments and empty lines, extracting token strings according
     * to the regular expression given at construction.  The tokens are collected, in order, and returned in
     * a string array <code>String[]</code> compatible with the Java <code>main(String[])</code> application 
     * entry point.
     * </p>
     *  
     * @param brFile    text buffer containing the contents of file with application command-line arguments
     * 
     * @return  string array of tokens matching the application command-line argument collection
     * 
     * @throws IOException                  unable to read buffer contents
     * @throws IllegalStateException        no match operation (i.e., {@link Matcher#find()}) attempted for the given matcher
     * @throws IndexOutOfBoundsException    attempted to read a pattern with index > {@link #arrRegex} length.
     */
    private String[]    parseLines(BufferedReader brFile) throws IOException, IllegalStateException, IndexOutOfBoundsException {
        
        // Container of parsed tokens
        List<String> lstTokens = new ArrayList<>();
        
        // Parse text buffer line-by-line skipping comments and empty lines
        String strLine;
        while ((strLine = brFile.readLine()) != null) {     // throws IOException

            String strTrimmed = strLine.trim();

            // Skip blank lines
            if (strTrimmed.isEmpty()) {
                continue;
            }

            // Skip comment lines 
            if (strTrimmed.startsWith(this.strComment)) {
                continue;
            }

            
            // Parse tokens according to regular expressions
            Matcher matcher = pattern.matcher(strLine);

            // Parse line for all tokens matching regular expression patterns defined at construction
            while (matcher.find()) {
                String strToken = this.extractPatternGroup(matcher);    // throws IllegalStateException, IndexOutOfBoundsException

//                if (matcher.group(1) != null) {     // throws IllegalStateException, IndexOutOfBoundsException
//                    strToken = matcher.group(1);
//                } else if (matcher.group(2) != null) {
//                    strToken = matcher.group(2);
//                } else {
//                    strToken = matcher.group(3);
//                }
                
                // Remove redundant escape characters
                strToken = this.removeEscapes(strToken);

                lstTokens.add(strToken);
            }
        }

        return lstTokens.toArray(new String[0]);
    }

    /**
     * <p>
     * Extracts the token from the given matcher according to the first pattern group encountered.
     * </p>
     * <p>
     * The method iterates through the pattern groups for the given matcher (assumed to contain the line of
     * text being parsed).  The given <code>Matcher</code> should be build from the current regular expression
     * parsing string <code>{@link #strRegex}</code>.
     * </p>
     * <p>
     * <h2>
     * <ul>
     * <li>The patterns are given by the array of regular expressions in <code>{@link #arrRegex}</code>.</li>
     * <li>The patterns are searched in the order of appearance in the above array.</li>
     * <li>If no patterns are found a <code>null</code> is returned.</li>
     * </ul>
     * 
     * @param mtchLine  <code>Matcher</code> object container the current parsing line
     * 
     * @return  the token corresponding to the regular expression group encountered first, or <code>null</code> if none
     * 
     * @throws  IllegalStateException       no match operation (i.e., {@link Matcher#find()}) attempted for the given matcher
     * @throws  IndexOutOfBoundsException   attempted to read a pattern with index > {@link #arrRegex} length.
     */
    private String  extractPatternGroup(Matcher mtchLine) throws IllegalStateException, IndexOutOfBoundsException {
        
        for (int iGrp=1; iGrp<this.cntGroups; iGrp++) {
            String  strToken = mtchLine.group(iGrp);    // throws IllegalStateException, IndexOutOfBoundsException
            
            if (strToken != null)
                return strToken;
        }
        
        return mtchLine.group(this.cntGroups);          // throws IllegalStateException, IndexOutOfBoundsException
    }
    
    /**
     * <p>
     * Removes the extraneous escape characters from the given argument.
     * </p>
     * <p>
     * This method performs a a cleanup operation on the given argument.  It removes the
     * multiple occurrences of escape character "\" from the given string.  These characters
     * can occur from parsing using the regular expression matcher.  
     * </p>
     * 
     * @param s     string with multiple escape characters for targets delimiters
     * 
     * @return      cleaned string
     */
    private String  removeEscapes(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }
    
    
    //
    // Code Example
    //
    
    /**
     * ChatGPT generated parser class. 
     *
     * @author Christopher K. Allen
     * @since Feb 14, 2026
     *
     */
    protected static class CommandLineParser {

        private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\"" +   // double-quoted with escapes
            "|'((?:\\\\.|[^'\\\\])*)'" +     // single-quoted with escapes
            "|(\\S+)"                        // unquoted token
        );

        public static String[] parse(Reader reader) throws IOException {
            List<String> tokens = new ArrayList<>();
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {

                String trimmed = line.trim();

                // Skip blank lines
                if (trimmed.isEmpty()) {
                    continue;
                }

                // Skip comment lines (first non-whitespace char is '#')
                if (trimmed.startsWith("#")) {
                    continue;
                }

                Matcher matcher = TOKEN_PATTERN.matcher(line);

                while (matcher.find()) {
                    String token;

                    if (matcher.group(1) != null) {
                        token = matcher.group(1);
                    } else if (matcher.group(2) != null) {
                        token = matcher.group(2);
                    } else {
                        token = matcher.group(3);
                    }

                    tokens.add(unescape(token));
                }
            }

            return tokens.toArray(new String[0]);
        }

        private static String unescape(String s) {
            return s.replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\");
        }
    }
    
}
