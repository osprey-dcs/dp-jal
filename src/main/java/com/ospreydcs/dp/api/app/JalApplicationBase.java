/*
 * Project: dp-api-common
 * File:	JalApplicationBase.java
 * Package: com.ospreydcs.dp.api.app
 * Type: 	JalApplicationBase
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
 * @since May 28, 2025
 *
 */
package com.ospreydcs.dp.api.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future.State;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Bases class for applications based upon the Java API Library.
 * </p>
 * <p>
 * Provides general methods and resources common for JAL applications. This can be used as a base class for
 * building applications using the Java API Library, providing a jump start for operations common to most
 * applications.
 * </p>
 * <p>
 * The base class provides 
 * <ul>
 * <li>Output stream creation (for application reports) <code>{@link #openOutputStream(String)}</code>. </li>
 * <li>Unique file name creation (e.g., for output streams) <code>{@link #createUniqueFileName()}</code>. </li>
 * <li>Report header creation (provides time and class info) <code>{@link #createReportHeader()}</code>. </li>
 * <li>Exception report for sub-class <code>{@link #reportTerminalException(Throwable)}</code>. </li>
 * <li>Exception reporting for main class <code>{@link #reportTerminalException(Class, Throwable)}</code>. </li>
 * <li>Common state management (see <code>{@link #hasRun()}, {@link #hasCompleted()}</code>. </li>
 * <li>Command-line parsing methods (prefixed with <code>parseAppArgs...()</code> (see below).</li>
 * </ul> 
 * </p>
 * <p>
 * <h2>State Variables</h2>
 * The class provides the state variables <code>{@link #bolRun}</code> and <code>{@link #bolCompleted}</code>
 * for use by subclasses to manage execution state.  They are available as a convenience as the public
 * methods <code>{@link #hasRun()}</code> and <code>{@link #hasCompleted()}</code> are available for state
 * inquiry.  Note that the state variables <em>must be</em> managed by the sub-class to provide correct state values.
 * The method <code>{@link #close()}</code> should be called to terminate the application before discarding
 * Sub-classes may wish to override this method to perform their own cleanup operations, or call the 
 * <code>{@link #close()}</code> method within their own termination process.
 * <p>
 * <p>
 * <h2>Output Stream</h2>
 * The base class contains the attribute <code>{@link #psOutput}</code> which is set by the method 
 * <code>{@link #openOutputStream(String)}</code>.  After opening the stream, with the given output location
 * (or {@value #STR_ARG_VAL_STDOUT}), the output stream <code>{@link #psOutput}</code> is available for
 * saving application output.  The method <code>{@link #createReportHeader()}</code> is available for 
 * producing a standard output report header for output files.  The attribute <code>{@link #pathOutFile}</code> 
 * is also available to the sub-class to determine the exact location of the output data.
 * </p>  
 * <p>
 * The method <code>{@link #close()}</code> should always be called to close
 * the output stream before terminating.  
 * </p>
 * <h2>Command-Line Parsing</h2>
 * Contains multiple useful methods for application command-line parsing (e.g., in main-method) .
 * These are static methods prefixed with <code>parseAppArgs...()</code>; they are static so that they can
 * be called from the application <code>main(String[])</code> method.
 * </p>
 * <p> 
 * The command line parsers look for command-line switches, "commands",
 * variables, and properties.  They also identify some standard special requests such as {@value #STR_ARG_HELP}
 * and {@value #STR_ARG_VERSION}.  They can be combined with main class exception report through
 * <code>{@link #reportTerminalException(Class, Throwable)}</code> and/or 
 * <code>{@link #reportTerminalErrorMessage(Class, String)}</code> to configure the application.
 * </p>
 * <p>
 * See the method documentation for further details on their function and operation.
 * <ul>
 * <li><code>{@link #parseAppArgsErrors(String[], int, List)}</code></li>
 * <li><code>{@link #parseAppArgsHelp(String[])}</code></li>
 * <li><code>{@link #parseAppArgsVersion(String[])}</code></li>
 * <li><code>{@link #parseAppArgsCommands(String[])}</code></li>
 * <li><code>{@link #parseAppArgsSwitch(String[], String)}</code></li>
 * <li><code>{@link #parseAppArgsVariable(String[], String)}</code></li>
 * <li><code>{@link #parseAppArgsProperty(String[], String)}</code></li>
 * <li><code>{@link #parseAppArgsTarget(String[])}</code></li>
 * </ul>
 * </p>
 * <p>
 * <h2>Logging</h2>
 * The base class expects a Log4j <code>{@link Logger}</code> instance to be created in the application sub-class.
 * The logger instance is used here to log warning and error events, specifically in the 
 * <code>{@link #reportTerminalException(Throwable)}</code> instance method.  Thus, the abstract methods 
 * <code>{@link #isLogging()}</code> and <code>{@link #getLogger()}</code> methods must be implemented to 
 * support these actions.  
 * </p>
 * <p>
 * To deactivate logging by this base class simply implement the <code>{@link #isLogging()}</code> method to
 * return <code>false</code>, then the <code>{@link #getLogger()}</code> method can return <code>null</code>.
 * </p>
 * 
 * @param   <T> the class type of the final child application 
 * 
 * @author Christopher K. Allen
 * @since May 28, 2025
 *
 */
public abstract class JalApplicationBase<T extends JalApplicationBase<T>> {

    
    //
    // Special Arguments and Values
    //

    /** Special application argument switch for help  - see {@link #parseAppArgsHelp(String[])} */
    public static final String      STR_ARG_HELP = "--help";
    
    /** Special application argument switch for version - see {@link #parseAppArgsVersion(String[])} */
    public static final String      STR_ARG_VERSION = "--version";

    /** Special application argument variable value for console output - see {@link #openOutputStream(String)} */
    public static final String      STR_ARG_VAL_STDOUT = "console";

    
    //
    // Argument Option Delimiters
    //
    
    /** Argument properties assignment delimiter - see {@link #parseAppArgsProperty(String[], String)} */
    public static final String      STR_ARG_ASSGN = "=";

    
    /** Single hyphen argument option delimiter */
    public static final String      STR_ARG_DELIMIT_1 = "-";
    
    /** Double hyphen argument option delimiter */
    public static final String      STR_ARG_DELIMIT_2 = "--";
    
    /** List of all valid argument option delimiters */
    public static final List<String> LST_ARG_DELIMITERS = List.of(STR_ARG_DELIMIT_1, STR_ARG_DELIMIT_2); 
    
    
    //
    // Default Values
    //
    
    /** The default output file extension - see {@link #openOutputStream(String)} */
    public static final String     STR_FILE_EXT_DEF = ".txt";

    
    //
    // Defining Attributes
    //
    
    /** The class type of the final application */
    protected final Class<T>        clsApp;


    //
    // Instance Resources
    //

    /** The unique file path if one was created - see {@link #createUniqueFileName()} */
    protected Path                  pathOutFile = null;
    
    /** The output stream to receive evaluation report if one was created (see {@link #openOutputStream(String)}) */
    protected PrintStream           psOutput = null;
    
    
    /** The timer task executor service */
    protected ScheduledExecutorService  execTimer = null; 
    
    /** The future for the timer task - used for task shut down */
    protected ScheduledFuture<?>        futTimer = null;

    
    //
    // State Variables
    //
    
    /** Evaluation execution attempted */
    protected boolean     bolRun = false;
    
    /** Evaluation completed flag */
    protected boolean     bolCompleted = false;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>JalApplicationBase</code> instance.
     * </p>
     *
     * @param clsApp    the class instance of the final application
     */
    protected JalApplicationBase(Class<T> clsApp) {
        this.clsApp = clsApp;
    }

    
    //
    // Abstract Methods
    //
    
    /**
     * <p>
     * Determine whether or not logging is active in the subclass.
     * </p>
     * 
     * @return  <code>true</code> the subclass is actively logging, <code>false</code> otherwise
     */
    abstract protected boolean  isLogging(); 
    
    /**
     * <p>
     * Get the Log4j <code>Logger</code> instance used by the subclass.
     * </p>
     * 
     * @return  class event logger
     */
    abstract protected Logger    getLogger();
    
    
    //
    // State/Attribute Query
    //
    
    /**
     * <p>
     * Determines whether or not the test suite has been run.
     * </p>
     * <p>
     * A return value of <code>true</code> indicates that the <code>{@link #run()}</code> method has been invoked
     * This method can be invoked only once so a <code>true</code> value indicates that the current evaluator is
     * in its final state and any results obtained are available.
     * </p>  
     * <p>
     * To determine if all test cases where evaluated successfully one must
     * invoke <code>{@link #hasCompleted()}</code>.  In other words, a returned value of <code>true</code> is 
     * necessary for evaluation success but not sufficient.
     * </p>
     * 
     * @return  <code>true</code> the <code>{@link #run()}</code> method has been invoked
     *          <code>false</code> the evaluator is still in its initial state
     */
    public boolean  hasRun() {
        return this.bolRun;
    }
    
    /**
     * <p>
     * Determines whether or not the test suite has been run and completed successfully.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the <code>{@link #run()}</code> method has been
     * invoked and that all test cases where successfully completed, specifically, there were no errors in
     * the <code>QueryChannel</code> evaluations performed by <code>{@link #run()}</code>.  
     * To determine if errors occurred and the <code>{@link #run()}</code>
     * method exited prematurely see <code>{@link #hasRun()}</code> which returns <code>true</code> in that case.
     * </p>
     *  
     * @return  <code>true</code> all test cases where successfully run,
     *          <code>false</code> either the evaluator has not be run or an error occurred during evaluations
     *          
     * @see #hasRun()
     */
    public boolean  hasCompleted() {
        return this.bolCompleted;
    }
    
    /**
     * <p>
     * Returns the output file path if one was created, or <code>null</code> if not.
     * </p>
     * 
     * @return  the output file path used by the application
     */
    public Path     getOutputFilePath() {
        return this.pathOutFile;
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Closes the application.
     * </p>
     * <p>
     * This method should be called before the application is discarded, specifically, at the end of its
     * lifetime.  The output stream is flushed and closed.
     * This is a blocking operation and does not return until all resources are released.
     * </p>
     * <p>
     * If the application has already been shut down (i.e., this method has already been called) nothing is done.
     * That is, it is safe to call this method multiple times.
     * </p>
     * 
     */
    public void close() {
        
        // Check if counter executor is running
        if (this.execTimer!=null && !this.execTimer.isTerminated()) {
            this.execTimer.shutdownNow();
        }
        
        // Check if output stream is open
        if (this.psOutput!=null && this.psOutput!=System.out) {
            this.psOutput.flush();
            this.psOutput.close();
            
            this.psOutput = null;
        }
    }

    
    //
    // Subclass Support Methods
    //
    
    /**
     * <p>
     * Starts the application timer task.
     * </p>
     * <p>
     * The "application timer" is a convenience for application clients; it simply prints out a "." to the
     * standard output (i.e., <code>{@link System#out}</code>) at the period given by the arguments.  It is
     * intended to indicate the the application is currently executing when long execution intervals are 
     * expected.
     * </p>
     * <p>
     * The timer task executes on a separate thread and must be explicitly shut down using method
     * <code>{@link #stopExecutionTimer()}</code>.  Note that the timer should not be used if CPU resources
     * are critical for application execution, as a <code> 
     * 
     * @param lngPeriod
     * @param tuPeriod
     * @throws IllegalStateException
     */
    protected void  startExecutionTimer(long lngPeriod, TimeUnit tuPeriod) throws IllegalStateException {
        
        // Check state
        if (this.execTimer!=null && !this.execTimer.isShutdown())
            throw new IllegalStateException("The execution timer is already running.");
        
        // Create counter task
        Runnable taskCntr = () -> System.out.print(".");

        // Create the timer executor and launch task
        this.execTimer = Executors.newScheduledThreadPool(1);
        this.futTimer = this.execTimer.scheduleAtFixedRate(taskCntr, lngPeriod, lngPeriod, tuPeriod);
    }
    
    /**
     * <p>
     * Stops the application timer task if it is running.
     * </p>
     * <p>
     * If the application timer has been started using the <code>{@link #startExecutionTimer(long, TimeUnit)}</code>
     * method, it is shutdown here and a value <code>true</code> is returned.  If the application timer has not been
     * started or it is already shut down then nothing happens and the method returns false.
     * </p>
     *   
     * @return  <code>true</code> if the application timer is shut down, <code>false</code> otherwise
     */
    protected boolean stopExecutionTimer() {
        
        if (this.futTimer==null || this.execTimer==null)
            return false;
        
        if (this.futTimer.state()==State.CANCELLED)
            return false;
        
        if (this.execTimer.isShutdown())
            return false;
        
        this.futTimer.cancel(true);
        this.execTimer.shutdown();
        return true;
    }

    /**
     * <p>
     * Opens the output stream connected to a new output file at the given path location.
     * </p>
     * <p>
     * A new output file is created with name given by <code>{@link #createUniqueFileName()}</code>. 
     * The time instant addition avoids collisions with any other output files at the location given
     * A new <code>PrintStream</code> object is created that is attached to the above file.  
     * by the argument.  
     * </p>
     * <p>
     * The new output stream is assigned to the attribute <code>{@link #psOutput}</code>, it is also
     * returned as a convenience for super classes.  The the output stream <code>{@link #psOutput}</code>
     * has already been opened (i.e., non-null) then it is closed and a new one is opened.
     * <p>
     * <h2>Special Case - StdOut</h2>
     * If the argument has special value {@value #STR_ARG_VAL_STDOUT} then the return value is the standard
     * output stream given by <code>{@link System#out}</code>.  In the Java default configuration this
     * directs the stream output to the console.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned stream should be closed when no longer needed (i.e., if not <code>{@link System#out}</code>.
     * </p> 
     * 
     * @param strPath   path location for the new output file
     * 
     * @return  a new output print stream connected to the new output file
     * 
     * @throws UnsupportedOperationException    file path is not associated with default provider
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to file
     */
    protected PrintStream  openOutputStream(String strPath) 
            throws UnsupportedOperationException, FileNotFoundException, SecurityException {

        // Check if stream is already opened
        if (this.psOutput!=null && this.psOutput!=System.out) {
            this.psOutput.flush();
            this.psOutput.close();
        }
            
        // Check if argument is "console"
        String  strConsole = strPath.toLowerCase();
        if (strConsole.equals(STR_ARG_VAL_STDOUT))
            return System.out;
        
        // Check if argument is directory - if so create unique file name
        this.pathOutFile = Path.of(strPath);
        
        if (Files.isDirectory(this.pathOutFile)) {
            String  strFileName = this.createUniqueFileName();
            
            this.pathOutFile = Paths.get(strPath, strFileName);
        } 
        
        // Open the common output file
        File        fileOutput = this.pathOutFile.toFile();     // throws UnsupportedOperationException
        this.psOutput = new PrintStream(fileOutput);            // throws FileNotFoundException, SecurityException
        
        // Return new stream connected to new output file
        return this.psOutput;
    }
    
    /**
     * <p>
     * Creates a unique file name based upon client class type and current time with default file extension.
     * </p>
     * <p>
     * The returned file name is constructed from the class simple name, the current time instant,
     * and the default file extension <code>{@link #STR_FILE_EXT_DEF}</code>.
     * </p>
     * 
     * @return  new, unique, file name with default extension 
     */
    protected String   createUniqueFileName() {
        return this.createUniqueFileName(STR_FILE_EXT_DEF);
    }
    
    /**
     * <p>
     * Creates a unique file name based upon client class type and current time.
     * </p>
     * <p>
     * The returned file name is constructed from the class simple name, the current time instant,
     * and the given file extension.
     * </p>
     * 
     * @return  new, unique, file name with given extension
     */
    protected String    createUniqueFileName(String strFileExt) {
        String      strCls = clsApp.getSimpleName();
        Instant     insNow = Instant.now();
        String      strNow = insNow.toString();
        String      strExt = strFileExt;
        
        String  strFileNm  = strCls + "-" + strNow + strExt;
        
        return strFileNm;
        
    }
    
    /**
     * <p>
     * Creates and returns a header line for evaluation reports.
     * </p>
     * <p>
     * A header is created which identifies the tool application and time of creation.
     * </p>
     * 
     * @return  a header string for evaluations reports
     */
    protected String   createReportHeader() {
        
        // Write header
        String      strHdr1 = this.pathOutFile.toAbsolutePath().toString();
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        String      strHdr2 = this.clsApp.getSimpleName() + " Output Results: " + strDateTime + "\n";

        return strHdr1 + "\n" + strHdr2;
    }
    
    /**
     * <p>
     * Reports a terminating exception to Standard Error and to the event logger if enabled (as error).
     * </p>
     * <p>
     * Reports a terminating exception (from the caller location) to both the Standard Error and to the current 
     * class logger via <code>{@link #getLogger()}</code>.  The Error message assumes a terminal condition and reports
     * it as such, reporting that the application is exiting.
     * </p>
     * 
     * @param e the exception to report
     */
    protected void reportTerminalException(Throwable e) {
        String  strHdr = JavaRuntime.getQualifiedCallerNameSimple() + " - TERMINAL ERROR.";
        String  strMsg = "Encountered exception " + e.getClass().getSimpleName() + ": " + e.getMessage() + ".";
        String  strBye = "Unable to continue. Exiting...";
        
        System.err.println(strHdr);
        System.err.println(strMsg);
        System.err.println(strBye);
        
        if (this.isLogging())
            this.getLogger().error(strMsg);
    }
    
    
    //
    // Main Method Support
    //
    
    /**
     * <p>
     * Parses the application argument collection for a help request.
     * </p>
     * <p>
     * A value <code>true</code> is returned if any element in the argument collection is equal to the value
     * {@value #STR_ARG_HELP}, where case is ignored.  Otherwise a value <code>false</code> is returned.
     * </p>
     * <p>
     * This method is equivalent to <code>{@link #parseAppArgsSwitch(String[], String)}</code> with the second
     * argument value as {@value #STR_ARG_HELP}.
     * </p>
     * 
     * @param args  the application argument collection
     *  
     * @return  <code>true</code> if the argument collection contained the element {@value #STR_ARG_HELP} (case ignored),
     *          <code>false</code> otherwise
     */
    public static boolean parseAppArgsHelp(String[] args) {
        
        // Check argument
        if (args==null)
            return false;
        
        // Look for help request
        boolean bolHelp = parseAppArgsSwitch(args, STR_ARG_HELP);
        
        return bolHelp;
    }
    
    /**
     * <p>
     * Parses the application argument collection for a version request.
     * </p>
     * <p>
     * A value <code>true</code> is returned if any element in the argument collection is equal to the value
     * {@value #STR_ARG_VERSION}, where case is ignored.  Otherwise a value <code>false</code> is returned.
     * </p>
     * <p>
     * This method is equivalent to <code>{@link #parseAppArgsSwitch(String[], String)}</code> with the second
     * argument value as {@value #STR_ARG_VERSION}.
     * </p>
     * 
     * @param args  the application argument collection
     *  
     * @return  <code>true</code> if the argument collection contained the element {@value #STR_ARG_VERSION} (case ignored),
     *          <code>false</code> otherwise
     */
    public static boolean    parseAppArgsVersion(String[] args) {
        
        // Check argument
        if (args==null)
            return false;
        
        // Look for version request
        boolean bolHelp = parseAppArgsSwitch(args, STR_ARG_VERSION);
        
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
     * <li>A {@value #STR_ARG_HELP} appeared in the argument list (<code>IllegalCallerException</code>).</li>
     * <li>A {@value #STR_ARG_VERSION} appeared in the argument list (<code>IllegalCallerException</code>).</li>
     * <li>An argument did not start with a valid switch/variable identified in argument (<code>UnsupportedOperationException</code>).</li>
     * </ol>
     * </p>
     * 
     * @param args          the application argument list 
     * @param cntMinArgs    the minimum number of required arguments to the application
     * @param lstDelOpts    list of valid application switches, variables, and properties with their delimiters (e.g., '-o', '--copy', etc.) 
     * 
     * @throws IllegalArgumentException         wrong number of arguments (did not contain enough commands/options)
     * @throws IllegalCallerException           the client request application help or version message
     * @throws UnsupportedOperationException    an application argument contained an invalid option flag
     */
    public static void parseAppArgsErrors(String[] args, int cntMinArgs, List<String> lstDelOpts) 
            throws NoSuchElementException, IllegalCallerException, UnsupportedOperationException {

        // Check the argument count
        if (args==null || args.length < cntMinArgs)
            throw new IllegalArgumentException("The argument list " + args + " has lenth less than minimum " + cntMinArgs);
        
        // Check for help request
        boolean bolHelp = Arrays.asList(args).stream().anyMatch(arg -> arg.strip().equalsIgnoreCase(STR_ARG_HELP));
        if (bolHelp)
            throw new IllegalCallerException("The client requested help message.");
        
        // Check for version request
        boolean bolVersion = Arrays.asList(args).stream().anyMatch(arg -> arg.strip().equalsIgnoreCase(STR_ARG_VERSION));
        if (bolVersion)
            throw new IllegalCallerException("The client requested version information.");
        
        // Check each argument for valid flag
        for (String strToken : args) {
            String strArg = strToken.strip();
            
            // Check if argument is delimited - if not move to the next one
            boolean bolDelimited = LST_ARG_DELIMITERS.stream().anyMatch(s -> strArg.contains(s));
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
     * Parses the given argument collection for the appearance of the given command switch.
     * </p>
     * <p>
     * This method determines whether or not the given switch is present within the given application argument
     * collection, returning <code>true</code> if found.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Command line "switches" are arguments that appear without parameters, typically instructing the
     * application to conform to a given configuration or to perform a specific action.  They are generally
     * marked with delimiters '-' or '--'.
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
     * @param args          the application argument collection
     * @param strSwitch     the command line switch searched for
     * 
     * @return  <code>true</code> if the given switch was found in the argument collection, <code>false</code> otherwise
     */
    public static boolean    parseAppArgsSwitch(String[] args, String strSwitch) {
        
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
     * where <code>command1 ... commandN</code> are the commands.  This method returns all application arguments
     * recognized as commands in the order in which they appear on the command line. 
     * </p>
     * 
     * @param args  application argument collection
     * 
     * @return  ordered list of application commands
     */
    public static List<String>   parseAppArgsCommands(String[] args) {
        
        // Check arguments
        if (args==null || args.length<1)
            return List.of();

        // The returned list of commands
        List<String>    lstCmds = new LinkedList<>();
        
        for (String strToken : args) {
            String  strArg = strToken.strip();
            
            // Look for delimiters - if found we are done with commands
            boolean bolDelimited = LST_ARG_DELIMITERS.stream().anyMatch(strDel -> strArg.startsWith(strDel)); 
            if (bolDelimited)
                break;
            
            lstCmds.add(strArg);
        }
        
        return lstCmds;
    }

    /**
     * <p>
     * Parses the application argument collection for all variable values with the given delimiter.
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
     * 
     * @param args      application argument collection
     * @param strDelVar the command-line variable with delimiter
     * 
     * @return  ordered list of variable values taken from the command line arguments
     */
    public static List<String>   parseAppArgsVariable(String[] args, String strDelVar) {
        
        // Check arguments
        if (args==null || args.length<1)
            return List.of();

        // Count the number of variable occurrences
        int cntOccur = Arrays.asList(args)
                .stream()
                .<String>map(arg -> arg.strip())
                .filter(s -> s.startsWith(strDelVar))
                .mapToInt(s -> 1)
                .sum();
        
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

            // Keep read variable values until next delimiter, or end of arguments
            for (int i=iArg; i<args.length; i++) {
                String  strToken = args[i].strip();

                // If we find a delimiter we are at the end of the variable list
                boolean bolEnd = LST_ARG_DELIMITERS.stream().anyMatch(strDel -> strToken.startsWith(strDel));
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
     * Property assignments within the application command line appear a (name, value) pairs separated by the
     * assignment operation {@value #STR_ARG_ASSGN}.  Their property type is identified with a zero white space
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
     * </p>
     * <p>
     * To launch application <code>MyApp</code> supplying it with a property with delimiter '-P' and supplying the
     * Java VM with the system properties as before one would invoke the following:
     * <pre>
     * <code>
     *   %java -Dprop1=val1 ... -DpropN=valN MyApp -PmyProp=myVal
     * </code>
     * </pre>
     * where <code>'myProp'</code> is the property name and <code>'myVal'</code> is the property value.  Of course
     * any delimiter may be chosen, which has context within the application.
     * </p>
     * <p>
     * The method returns the (name, value) pairs as (key, value) entries within the returned map.  If no properties
     * with the given delimiter are found an empty map is returned.  If a property with a bad assignment format is
     * found an exception is thrown.
     * </p>
     * 
     * @param args          application argument collection
     * @param strDelProp    the command-line property delimiter
     * 
     * @return  map of (name, value) properties for the given property delimiter
     * 
     * @throws ConfigurationException   the property assignment format was bad 
     */
    public static Map<String, String>   parseAppArgsProperty(String[] args, String strDelProp) throws ConfigurationException {
        
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
                String[]    arrTokens = strElem.split(STR_ARG_ASSGN);        // split at assignment
                
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
     * The "target" of an application, if present, is generally the last argument in the argument collection.
     * Of course this operation only has meaning in the context of the application where the last argument is
     * neither a command or a variable value.  This method simply selects the last argument in the argument collection
     * and returns it, so long as it does not start with a delimiter in the collection <code>{@link #LST_ARG_DELIMITERS}</code>.
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
    public static String    parseAppArgsTarget(String[] args) {
        
        // Check arguments
        if (args==null || args.length<1)
            return null;
        
        // Get the last argument
        int     cntArgs = args.length;
        String  strArgLast = args[cntArgs - 1];
        
        // Check if delimiter exists
        boolean bolDelimited = LST_ARG_DELIMITERS.stream().anyMatch(strDel -> strArgLast.startsWith(strDel));
        if (bolDelimited)
            return null;
        
        return strArgLast;
    }
    
    /**
     * <p>
     * Reports a terminating error to Standard Error.
     * </p>
     * <p>
     * Reports a terminating exception (from the caller location) to the Standard Error.
     * The Error message assumes a terminal condition and reports it as such, reporting that the 
     * application is exiting.
     * </p>
     * <p>
     * This method is intended to be called from a <code>main</code> method application entry point
     * upon discovery of terminating error (e.g., bad command-line arguments). 
     * A <code>{@link System#exit(int)}</code> is assumed to be called following this method invocation.
     * </p>
     * 
     * @param <T>   the type of the application calling this method
     * 
     * @param clsApp    the class type of the application
     * @param e         the exception to report
     */
    public static <T extends JalApplicationBase<T>> void  reportTerminalException(Class<T> clsApp, Throwable e) {
        String  strApp = clsApp.getClass().getName() + " TERMINAL ERROR at ";
        String  strHdr = JavaRuntime.getQualifiedCallerNameSimple() + ".";
        String  strMsg = "Encountered exception " + e.getClass().getSimpleName() + ": " + e.getMessage() + ".";
        String  strBye = "Unable to continue. Exiting...";
        
        System.err.println(strApp);
        System.err.println(strHdr);
        System.err.println(strMsg);
        System.err.println(strBye);
    }
    
    /**
     * <p>
     * Reports a terminating error message to Standard Error.
     * </p>
     * <p>
     * Reports a terminating message (from the caller location) to the Standard Error.
     * The message assumes a terminal condition and reports it as such, reporting that the 
     * application is exiting.
     * </p>
     * <p>
     * This method is intended to be called from a <code>main</code> method application entry point
     * upon discovery of terminating error (e.g., bad command-line arguments). 
     * A <code>{@link System#exit(int)}</code> is assumed to be called following this method invocation.
     * </p>
     * 
     * @param <T>   the type of the application calling this method
     * 
     * @param clsApp    the class type of the application
     * @param strMsg    the message to report
     */
    public static <T extends JalApplicationBase<T>> void  reportTerminalErrorMessage(Class<T> clsApp, String strMsg) {
        String  strApp = clsApp.getClass().getName() + " TERMINAL ERROR at ";
        String  strHdr = JavaRuntime.getQualifiedCallerNameSimple() + ".";
        String  strBye = "Unable to continue. Exiting...";
        
        System.err.println(strApp);
        System.err.println(strHdr);
        System.err.println(strMsg);
        System.err.println(strBye);
    }
    
}
