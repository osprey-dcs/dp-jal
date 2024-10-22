/*
 * Project: dp-api-common
 * File:	Log4j.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	Log4j
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
 * @since Oct 22, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.StandardLevel;


/**
 * <h1>
 * Utility class for managing the Apache Logging Library Log4j
 * </h1>
 * <p>
 * Contains methods for creating and configuring Apache Logger objects beyond the standard configuration
 * mechanism (e.g., with the <em>log4j2.xml</em> resource file).  Also contains methods for creating logging 
 * utilities such as log "appenders."
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Oct 22, 2024
 *
 */
public class Log4j {
    
    
    //
    // Class Constants
    //
    
    /** The default logger pattern string (Log4j PatternLayout) used for appenders when none is given */ 
    private static final String STR_LOG_PATTERN_DEF = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n";

    
    /**
     * <h1>
     * Gets the logger with the given name or creates a new logger if the named logger does not exist
     * </h1> 
     * <p>
     * A Log4j logger instance is created with the argument name if no loggers with that name currently exist.
     * Otherwise, the named logger is returned.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Depending upon the pattern layout within the returned logger its name may appear in all output log entries.
     * </p>
     * 
     * @param strName   name of the returned logger
     * 
     * @return  logger with given name, or new logger if none by that name exists
     */
    public static Logger    getLogger(String strName) {
        
        Logger  logger = LogManager.getLogger(strName);
        
        return logger;
    }
    
    /**
     * <h1>
     * Sets the 'logging level' for the given logger to the given level
     * </h1>
     * <p>
     * Defers to <code>{@link #convertLogLevel(StandardLevel)}</code> to convert the given
     * <code>StandardLevel</code> enumeration constant to its <code>Level</code> class constant
     * then invokes <code>{@link #setLevel(Logger, Level)}</code> to modify the log level.
     * </p> 
     * 
     * @param logger    logger whose level is to be modified
     * @param enmLevel  new log level for the given logger
     * 
     * @throws TypeNotPresentException  the given enumeration constant has no Level equivalent
     *
     * @see #convertLogLevel(StandardLevel)
     * @see #setLevel(Logger, Level)
     * @see StandardLevel
     */
    public static void setLevel(Logger logger, StandardLevel enmLevel) throws TypeNotPresentException {
        
        // Convert to Level class constant and configure
        Level   level = Log4j.convertLogLevel(enmLevel);
        
        Log4j.setLevel(logger, level);
    }

    /**
     * <h1>
     * Sets the 'logging level' for the given logger to the given level
     * </h1>
     * <p>
     * Log4j loggers can be configured to output log entries by degree (e.g., DEBUG, INFO, WARN, etc.).  
     * This method sets the given logger to output log entries that equal or exceed the given threshold
     * given by the <code>level</code> argument.  See the class <code>{@link Level}</code> for the
     * available logging levels and their names.  Note that the actual levels are specified by constant
     * enclosed class instances within the <code>Level</code> class.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The operation performed here will override any default logging levels set within the  Log4j configuration
     * file <em>log4j2.xml</em>.
     * </p>
     * 
     * @param logger    logger whose level is to be modified
     * @param level     new log level for given logger
     * 
     * @see Level
     */
    public static void setLevel(Logger logger, Level level) {
        
        // Set the logging level using the Log4j 'Configurator'
        Configurator.setLevel(logger, level);
    }
    
    /**
     * <h1>Converts a <code>StandardLevel</code> enumeration to a <code>Level</code> class instance.</h1>
     * <p>
     * Convenience method for converting the Log4j enumeration constants <code>{@link StandardLevel}</code>
     * to their corresponding <code>{@link Level}</code> enclosed class instances.  The <code>Level</code>
     * class instances are accepted as level configuration parameters for the Log4j logger 
     * <code>{@link Configurator}</code> class methods.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>StandardLevel</code> enumeration constant for a given <code>Level</code> constant class
     * can be recovered using <code>{@link Level#getStandardLevel()}</code>.
     *   
     * @param enmLevel  a <code>StandardLevel</code> enumeration constant
     * 
     * @return  the <code>Level</code> enclosed static class instance corresponding to the argument
     * 
     * @throws TypeNotPresentException  the given enumeration constant has no Level equivalent
     * 
     * @see StandardLevel
     */
    public static Level   convertLogLevel(StandardLevel enmLevel) throws TypeNotPresentException {
        
        Level   level = switch (enmLevel) {
        case OFF -> Level.OFF;
        case FATAL -> Level.FATAL;
        case ERROR -> Level.ERROR;
        case WARN -> Level.WARN;
        case INFO -> Level.INFO;
        case DEBUG -> Level.DEBUG;
        case TRACE -> Level.TRACE;
        case ALL -> Level.ALL;
        default -> throw new TypeNotPresentException(enmLevel.name(), new Throwable("StandardLevel enumeration not found and/or supported."));
        };
        
        return level;
    }

    /**
     * <h1>
     * Creates and returns a new file appender for attachment to a Log4j Logger object
     * </h1>
     * <p>
     * Defers to <code>{@link #createFileAppender(String, String, String)}</code> using value
     * <code>{@link #STR_LOG_PATTERN_DEF}</code> as the pattern layout for log entries.
     * </p>
     * 
     * @param strName       the unique name of the file appender
     * @param strFile       the path and file name of the logging output file
     * 
     * @return  Log4j file appender - persistent storage target for log entries
     * 
     * @see #createFileAppender(String, String, String)
     */
    public static FileAppender  createFileAppender(String strName, String strFile) {
        return Log4j.createFileAppender(strName, strFile, STR_LOG_PATTERN_DEF);
    }
    
    /**
     * <h1>
     * Creates and returns a new file appender for attachment to a Log4j Logger object
     * </h1>
     * <p>
     * Defers to <code>{@link #createFileAppender(String, String, String, boolean)}</code> setting 
     * argument <code>bolAppend</code> to <code>true</code>.  That is, the returned file appender
     * appends log entries to the end of the output file.
     * </p>
     * 
     * @param strName       the unique name of the file appender
     * @param strFile       the path and file name of the logging output file
     * @param strPattern    the pattern layout string specifying the format of each log entry
     * 
     * @return  Log4j file appender - persistent storage target for log entries
     * 
     * @see #createFileAppender(String, String, String, boolean)
     */
    public static FileAppender  createFileAppender(String strName, String strFile, String strPattern) {
        return Log4j.createFileAppender(strName, strFile, strPattern, true);
    }
    
    /**
     * <h1>
     * Creates and returns the target of the scenario event logging according to the scenario configuration
     * </h1>
     * <p>
     * Creates and returns a Log4j <code>@link FileAppender</code> which saves log entries to the file location
     * given in the argument.  The appender is configured according to the argument parameters. 
     * </p> 
     * <p>
     * <h2>Output File</h2>
     * The output file path and name is given as a single string.  Either an absolute path or relative path may be 
     * given.
     * </p>
     * <p>
     * <h2>Pattern Layouts</h2>
     * Log entries are formatted according to the <em>pattern layout</em> string.  These strings are closely related
     * to the C <code>printf</code> function format string.  For more information on pattern layout strings see
     * the documentation for class <code>{@link org.apache.logging.log4j.core.layout.PatternLayout}</code> and
     * the online documentation <a href=https://logging.apache.org/log4j/2.x/manual/layouts.html>Log4j Layouts</a>.
     * </p>
     * <p>
     * <h2>Appender Attachment</h2>
     * The returned appender instance must be attached to an logger instance for the appender to function.  The method
     * <code>{@link #attachAppender(Logger, AbstractAppender)}</code> is provided for this operation.  It is not
     * possible to directly attach an appender to the standard <code>Logger</code> object available from the
     * Log4j <code>LogManager</code>.
     * </p>
     * 
     * @param strName       the unique name of the file appender
     * @param strFile       the path and file name of the logging output file
     * @param strPattern    the pattern layout string specifying the format of each log entry
     * @param bolAppend     append log entries to the end of the file (assures atomic writes for multiple applications) 
     * 
     * @return  Log4j file appender - persistent storage target for log entries
     */
    public static FileAppender    createFileAppender(String strName, String strFile, String strPattern, boolean bolAppend) {

        // Create the pattern layout
        PatternLayout.Builder   bldrLayout = PatternLayout.newBuilder();
        bldrLayout.withPattern(strPattern);
        PatternLayout layout = bldrLayout.build(); 

        FileAppender.Builder<?> bldrFile = FileAppender.newBuilder();
        bldrFile.setName(strName);
        bldrFile.setLayout(layout);
        bldrFile.withFileName(strFile);
        bldrFile.withAppend(bolAppend);
        bldrFile.withCreateOnDemand(false);

        // Create the file appender and start it
        FileAppender    appendFile = bldrFile.build();
        appendFile.start();

        return appendFile;
    }
    
    /**
     * <h1>
     * Attaches the given Log4j appender to the given Logger object
     * </h1>
     * <p>
     * The common Log4j <code>Logger</code> objects are actually interfaces which do provide operations for
     * appender attachment.  This method casts the given argument to its concrete implementation which contains
     * methods for appender attachment.
     * </p> 
     * 
     * @param logger    target logger to received appender attachment
     * @param appender  appender to be added to target logger
     * 
     * @return  <code>true</code> if the appender was successfully attached, <code>false</code> otherwise
     */
    public static boolean    attachAppender(Logger logger, AbstractAppender appender) {
        
        try {
            // Cast logger interface to its implementation 
            org.apache.logging.log4j.core.Logger    loggerImpl = (org.apache.logging.log4j.core.Logger)logger;

            // Attach the appender to target logger
            loggerImpl.addAppender(appender);
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prevent instance construction of <code>Log4j</code>.
     * </p>
     *
     */
    private Log4j() {
    }

}
