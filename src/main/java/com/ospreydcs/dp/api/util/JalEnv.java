/*
 * Project: dp-api-common
 * File:	JalEnv.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	JalEnv
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
 * @since May 23, 2025
 *
 */
package com.ospreydcs.dp.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Utility class for managing the Java API Library environment.
 * </p>
 * <p>
 * The Data Platform Java API Library (JAL) anticipates the system environment variable {@value #STR_VAR_ENV_JAL_HOME}
 * at the time of the Java Virtual Machine (VM) startup.
 * This variable contains the installation location for the Java API Library, say path <code>jal_home</code>.
 * This class is provided as a utility for managing JAL resources within that environment during the lifetime
 * of the current Java VM.
 * </p>
 * <p>
 * <h2>Locating {@value #STR_VAR_ENV_JAL_HOME}</h2>
 * There are multiple methods for locating the <code>{@value #STR_VAR_ENV_JAL_HOME}</code> environment variable. 
 * These methods exist in a hierarchy where each overrides the other.  The following lists the methods and
 * their hierarchy: 
 * <ol>
 * <li>
 * The system environment variable {@value #STR_VAR_ENV_JAL_HOME} - If this variable is defined in the system environment
 * (e.g., an 'export DP_API_JAVA_HOME=jav_install' statement) its value is taken.
 * </li>
 * <li>
 * The system property {@value #STR_VAR_ENV_JAL_HOME} - If this property is defined in the Java system properties
 * (e.g., with command line option java -D{@value #STR_VAR_ENV_JAL_HOME}=jal_install) its value is taken.
 * </li>
 * <li>
 * The system property {@value #STR_VAR_PROP_JAL_HOME} - If this property is defined in the Java system properties
 * (e.g., with command line option java -D{@value #STR_VAR_PROP_JAL_HOME}=jal_install) its value is taken.
 * </li>
 * The {@value #STR_FILE_NM_DP_ENV} file is parsed for variable {@value #STR_VAR_ENV_JAL_HOME} - The Data Platform environment
 * file {@value #STR_FILE_NM_DP_ENV} is searched for in the client's home directory.  If the {@value #STR_VAR_ENV_JAL_HOME}
 * variable is set there its value is taken.
 * </li> 
 * </ol>
 * Note that both the system property {@value #STR_VAR_ENV_JAL_HOME} and {@value #STR_VAR_PROP_JAL_HOME} are searched.
 * The latter property is included to conform to Java standards conventions.
 * </p>
 * <p>
 * <h2>JAL Configuration</h2>
 * This utility is particularly useful for JAL configuration management.  The library depends upon (YAML) configuration
 * files to assign default values for many of its components and resources.  The ability to dynamically change this
 * configuration would rely on the ability to access configuration files on the local platform, specifically within
 * the JAL installation base.  
 * </p>
 * <p>
 * Note that default configuration files are packaged within the Java API Library JAR files during library builds.
 * These configuration files are always available to the JAL but cannot be easily managed by JAL clients.
 * </p>  
 * <p>
 * <h2>JAL Directory Location and Creation</h2>
 * There are multiple methods for locating directories within the JAL installation identified by the 
 * {@value #STR_VAR_ENV_JAL_HOME} environment variable.  Additionally, directories at the JAL Home location can be
 * created.  The latter operations should be used with caution in order to avoid clobbering existing file structures.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * Most of the work is done by private method <code>{@link #establishJalEnv()}</code> which performs the search for the 
 * environment variable {@value #STR_VAR_ENV_JAL_HOME} described above.  Once the search have been performed all other
 * methods are dependent upon the result of that action.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 23, 2025
 *
 */
public class JalEnv {

    
    //
    // Class Constants - Library Resources
    //
    
    /** The Data Platform environment file (script) */
    public static final String      STR_FILE_NM_DP_ENV = ".dp.env"; 
    
    /** System environment variable containing Java API Library installation location */
    public static final String      STR_VAR_ENV_JAL_HOME = "DP_API_JAVA_HOME";
    
    /** System property name containing Java API Library installation location */
    public static final String      STR_VAR_PROP_JAL_HOME = "dp.api.java.home";

    
    //
    // Class Resources
    //
    
    /** The static logging utility */
    private static final Logger     LOGGER = LogManager.getLogger();

    
    //
    // Class Attributes
    //
    
    /** Value of the 'DP_API_JAVA_HOME` environment variable */
    private static String          STR_JAL_HOME = null;
    
    /** File system path specified by the 'DP_API_JAVA_HOME' environment variable (if set) */
    private static Path            PATH_JAL_HOME = null;

    
    //
    // Class State Variables
    //
    
    /** Flag indicating whether or not the Data Platform environment has been attempted */
    private static boolean         BOL_JAL_ENV_ATTEMPT = false;
    
    /** Flag indicating whether or not the Data Platform environment was established */
    private static boolean         BOL_JAL_ENV_ESTABLISH = false;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Determines whether or not the Java API Library system variable {@value #STR_VAR_ENV_JAL_HOME} was defined 
     * within the system environment at the time of Java Virtual Machine startup.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the system variable {@value #STR_VAR_ENV_JAL_HOME} was
     * defined and located at the time of the Java Virtual Machine startup.  Thus, the Java API Library environment 
     * has been defined and all public methods within this utility are operational 
     * (i.e., will not throw a <code>FileSystemNotFoundException</code>).
     * Otherwise (i.e., a returned value of <code>false</code>) the utility class is essentially useless.
     * </p>
     * 
     * @return  <code>true</code> if the Java API Library environment has been established and all public methods are operational,
     *          <code>false</code> otherwise
     */
    public static boolean   isJalEnvDefined() {
        return establishJalEnv();
    }
    
    /**
     * <p>
     * Returns the value of Java API Library system variable {@value #STR_VAR_ENV_JAL_HOME}.
     * </p>
     * <p>
     * System variable {@value #STR_VAR_ENV_JAL_HOME} should be set to the installation location of the Java API Library,
     * say <code>jal_home</code>.  This method returns the string value of that variable, or <code>null</code> if the
     * system variable was not set.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * A returned value of <code>null</code> indicates that the Java API Library environment has not been established
     * for this Java Virtual Machine and, therefore, none of the public methods within this utility are operational.
     * </p> 
     * 
     * @return  the string value of environment variable {@value #STR_VAR_ENV_JAL_HOME}, 
     *          or <code>null</code> if the environment variable was not set.
     */
    public static String    getJalHomeValue() {
        
        // Check if environment is established
        boolean bolEnv = establishJalEnv();
        
        if (bolEnv)
            return STR_JAL_HOME;
        else
            return null;
    }
    
    /**
     * <p>
     * Returns the path to the Java API Library installation directory (i.e., JAL Home).
     * </p>
     * <p>
     * The returned path is constructed from the value of system environment variable {@value #STR_VAR_ENV_JAL_HOME}.
     * This variable should be set to the location of the Java API Library installation on the local platform.
     * If the variable was set and the location is valid, its path is returned.  Otherwise an exception is
     * thrown.
     * </p>
     * 
     * @return  the directory (path) location of the Java API Library home directory
     *  
     * @throws FileSystemNotFoundException  either the system variable {@value #STR_VAR_ENV_JAL_HOME} was not set or was invalid
     */
    public static Path  getJalHomePath() throws FileSystemNotFoundException {
     
        // Check if environment is established
        checkJalEnvEstablished();   // throws FileSystemNotFoundException

        return PATH_JAL_HOME;
    }
    
    /**
     * <p>
     * Returns the path with the given path elements relative to the Java Library installation directory (i.e., JAL Home).
     * </p>
     * <p>
     * The returned path is constructed from the given path elements, which are relative to the Java API Library
     * installation directory, say <code>jal_install</code>.  For example, if the system path delimiter is '/' then
     * invoking <code>getJalHomePath("test", "output", "query")</code> would return the path to directory 
     * <code>'jal_install/test/output/query'</code>. 
     * </p>
     *  
     * @param strPathRel    the first path element relative to the JAL home directory
     * @param strPathElems  any additional path elements
     * 
     * @return  path relative the Java API Library installation directory built from the arguments
     * 
     * @throws FileSystemNotFoundException  the Java API Library was never established
     * @throws InvalidPathException         the path constructed by the arguments is invalid
     */
    public static Path  getJalHomePath(String strPathRel, String...strPathElems) throws FileSystemNotFoundException, InvalidPathException {
        
        Path pathHome = getJalHomePath();                           // throws FileSystemNotFoundException
        Path pathRel = pathHome.resolve(strPathRel, strPathElems);  // throws InvalidPathException
        
        return pathRel;
    }
    
    /**
     * <p>
     * Returns a file with the given path elements relative to the Java Library installation directory (i.e., JAL Home).
     * </p>
     * <p>
     * The returned file is constructed from the given path elements, which are relative to the Java API Library
     * installation directory, say <code>jal_install</code>.  For example, if the system path delimiter is '/' then
     * invoking <code>getJalHomeFile("test", "output", "query", "channel", "TestSuite1.txt")</code> would return 
     * the file object <code>'jal_install/test/output/query/channel/TestSuite1.txt'</code>.   
     * </p>
     * <p>
     * Note that the returned value is a Java <code>{@link File}</code> object.  Thus, the file object may or may not
     * exist on the file system.  It can be successfully opened for reading if it exists, or created for writing
     * assuming the directory path is valid.  See <code>{@link #createJalHomePath(String, String...)}</code> if a
     * relative path needs to be created.
     * </p>
     * 
     * @param strFirst  either a path element, or file name if second argument is empty (relative to JAL Home)
     * @param strMore   a sequence of path elements ending in a file name
     * 
     * @return  file relative the Java API Library installation directory built from the arguments
     * 
     * @throws FileSystemNotFoundException  the Java API Library was never established
     * @throws InvalidPathException         the path and/or file name constructed by the arguments is invalid
     */
    public static File  getJalHomeFile(String strFirst, String...strMore) throws FileSystemNotFoundException, InvalidPathException {
        
        Path    pathHome = getJalHomePath();                    // throws FileSystemNotFoundException
        Path    pathFile = pathHome.resolve(strFirst, strMore); // throws InvalidPathException
        File    file = pathFile.toFile();                   // throws UnsupportedOperationException
        
        return file;
    }
    
    /**
     * <p>
     * Creates a path and/or file with the given elements relative to the Java API installation directory (i.e., JAL Home).
     * </p>
     * <p>
     * Creates a new directory structure and/or file with the given elements with respect to the Java API Library
     * home directory.
     * Note that the returned file represents either a <b>new</b> directory structure, or file with potential new
     * directory structure.  If the directory/file already exists then an exception is thrown.
     * </p>
     * <p>
     * The arguments represent a sequence of path elements relative the Java API Library home directory, potentially
     * ending with a file name.   For example, if the system path delimiter is '/' then
     * invoking <code>createJalHomePath("test", "output", "query", "channel", "TestSuite1.txt")</code> would return 
     * the file object <code>'jal_install/test/output/query/channel/TestSuite1.txt'</code>.  This assumes that
     * the file <code>'TestSuite1.txt'</code> does not exist.  However, if any sub-directories within the
     * path <code>'jal_install/test/output/query/channel'</code> do not exist, they are created.
     * </p>
     * 
     * @param strFirst  either a path element, or file name if second argument is empty 
     * @param strMore   a sequence of path elements ending in a file name
     * 
     * @return  file relative the Java API Library installation directory built from the arguments
     * 
     * @throws FileSystemNotFoundException  the Java API Library was never established
     * @throws InvalidPathException         the path and/or file constructed by the arguments is invalid
     * @throws FileAlreadyExistsException   the file/directory constructed by the arguments already exits
     * @throws IOException                  unable to create the file/directory with the given elements
     */
    public static Path  createJalHomePath(String strPathRel, String... strPathElems) 
            throws FileSystemNotFoundException, InvalidPathException, FileAlreadyExistsException, IOException {
        
        Path pathJal = getJalHomePath(strPathRel, strPathElems);    // throws FileSystemNotFoundException, InvalidPathException
        
        // If file already exists throw exception
        if (Files.exists(pathJal))
            throw new FileAlreadyExistsException("File " + pathJal + " already exists.");
        
        Path pathNew = Files.createDirectories(pathJal);    // throws UnsupportedOperationException, FileAlreadyExistsException, IOException
        
        return pathNew;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Checks if the JAL environment has been successfully established.
     * </p>
     * <p>
     * This is a convenience method for throwing an exception whenever the JAL environment establishment
     * has failed.  It also logs a warning message indicating that {@value #STR_VAR_ENV_JAL_HOME} is unavailable.
     * This is provided as an option to calling <code>{@link #establishJalEnv()}</code> directly and
     * receiving a <code>boolean</code> result.
     * </p>  
     * <p>
     * This operation is useful whenever methods are expected to return a value which cannot be supplied if
     * the class variable <code>{@value #STR_JAL_HOME}</code> has not been set (i.e., the JAL environment
     * was not established).
     * </p>
     * <p>
     * This method also fails (i.e., throws an exception if the system variable {@value #STR_VAR_ENV_JAL_HOME}
     * was found but specifies an invalid directory location.  That is, the path pointed to by the
     * system variable is either an invalid directory or not the location of the JAL installation.
     * </p>
     * 
     * @throws FileSystemNotFoundException  the JAL environment was not successfully established
     */
    private static void checkJalEnvEstablished() throws FileSystemNotFoundException {
        
        // Check if environment is established
        boolean bolEnv = establishJalEnv();
        
        // If unable to locate DP_API_JAVA_HOME variable from environment
        if (!bolEnv) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Java API Library environment is NOT established - environment variable " 
                    + STR_VAR_ENV_JAL_HOME + " is unavailable.";

            LOGGER.warn(strMsg);

            throw new FileSystemNotFoundException(strMsg);
        }
        
        // Check if the PATH_JAL_HOME path is valid - this is a redundant operation (already checked in establishJalEnv())
        boolean bolPathValid = Files.isDirectory(PATH_JAL_HOME);
        
        if (!bolPathValid) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Java API Library (JAL) environment variable " + STR_VAR_ENV_JAL_HOME
                    + " with value " + STR_JAL_HOME
                    + " produces invalid JAL home directory " + PATH_JAL_HOME;
            
            LOGGER.warn(strMsg);
            
            throw new FileSystemNotFoundException(strMsg);
        }
    }
    
    /**
     * <p>
     * Attempts to establish the Java API Library environment variable {@value #STR_VAR_ENV_JAL_HOME}.
     * </p>
     * <p>
     * The <code>{@value #STR_VAR_ENV_JAL_HOME}</code> environment variable contains the location of the library
     * installation on the local platform, say directory <code>jal_install</code>.
     * The method attempts to locate the Java API Library environment variable <code>{@value #STR_VAR_ENV_JAL_HOME}</code>
     * amongst a standard location set available to the Java Virtual Machine (VM).  If found the variable value 
     * is set and maintained within this class for the lifetime of the Java VM.
     * </p>   
     * <p>
     * This method can be invoked multiple times but only performs the environment establishment process once.
     * Specifically, upon initial invocation the <code>{@value #STR_VAR_ENV_JAL_HOME}</code> environment variable is
     * sought out within the host platform and its value set if available.  Any following invocations 
     * simply return the result of the initial invocation (i.e., either <code>true</code> or <code>false</code>.
     * </p>
     * <p>
     * <h2>Setting {@value #STR_VAR_ENV_JAL_HOME}</h2>
     * There are multiple methods for establishing the <code>{@value #STR_VAR_ENV_JAL_HOME}</code> environment variable. 
     * These methods exist in a hierarchy where each overrides the other.  The following lists the methods and
     * their hierarchy: 
     * <ol>
     * <li>
     * The system environment variable {@value #STR_VAR_ENV_JAL_HOME} - If this variable is defined in the system environment
     * (e.g., an 'export DP_API_JAVA_HOME=jav_install' statement) its value is taken.
     * </li>
     * <li>
     * The system property {@value #STR_VAR_ENV_JAL_HOME} - If this property is defined in the Java system properties
     * (e.g., with command line option java -D{@value #STR_VAR_ENV_JAL_HOME}=jal_install) its value is taken.
     * </li>
     * <li>
     * The system property {@value #STR_VAR_PROP_JAL_HOME} - If this property is defined in the Java system properties
     * (e.g., with command line option java -D{@value #STR_VAR_PROP_JAL_HOME}=jal_install) its value is taken.
     * </li>
     * The {@value #STR_FILE_NM_DP_ENV} file is parsed for variable {@value #STR_VAR_ENV_JAL_HOME} - The Data Platform environment
     * file {@value #STR_FILE_NM_DP_ENV} is searched for in the client's home directory.  If the {@value #STR_VAR_ENV_JAL_HOME}
     * variable is set there its value is taken.
     * </li> 
     * </ol>
     * Note that both the system property {@value #STR_VAR_ENV_JAL_HOME} and {@value #STR_VAR_PROP_JAL_HOME} are searched.
     * The latter property is included to conform to Java standards conventions.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If all the above searches fail the <code>DP_API_JAVA_HOME</code> variable is left empty (i.e., <code>null</code>)
     * and the operation fails.
     * </li>
     * <li>
     * This method also sets the value of <code>{@link #PATH_JAL_HOME}</code> to the value of {@value #STR_VAR_ENV_JAL_HOME}
     * if the variable is found.  The value of <code>{@link #BOL_JAL_ENV_ESTABLISH}</code> then depends upon the validity
     * of this path.
     * </li>
     * </ul>
     * </p>
     * 
     * @return <code>true</code> if the <code>DP_API_JAVA_HOME</code> environment variable was found at its value set
     *         <code>false</code> otherwise
     */
    private static boolean establishJalEnv() {
        
        // Entry Gate - only run once
        if (BOL_JAL_ENV_ATTEMPT)
            return BOL_JAL_ENV_ESTABLISH;
        
        // Initialize 'established' flag and indicate that the attempt has been made
        BOL_JAL_ENV_ATTEMPT = true;
        BOL_JAL_ENV_ESTABLISH = false;
        
        // Look in the system environment variables 
        String  strEnvVal = System.getenv(STR_VAR_ENV_JAL_HOME);
        
        // Check if DP_API_JAVA_HOME is an environment variable 
        //  If so, we are done
        if (strEnvVal != null) {
            STR_JAL_HOME = strEnvVal;
            PATH_JAL_HOME = Path.of(strEnvVal);
            BOL_JAL_ENV_ESTABLISH = Files.isDirectory(PATH_JAL_HOME);
            
            return BOL_JAL_ENV_ESTABLISH;
        }
        
        // Check if DP_API_JAVA_HOME is a system property
        //  If so we are done
        String  strPropVal = System.getProperty(STR_VAR_ENV_JAL_HOME);
        if (strPropVal != null) {
            STR_JAL_HOME = strPropVal;
            PATH_JAL_HOME = Path.of(strPropVal);
            BOL_JAL_ENV_ESTABLISH = Files.isDirectory(PATH_JAL_HOME);
            
            return BOL_JAL_ENV_ESTABLISH;
        }
        
        // Check if dp.api.java.home is a system property
        //  If so we are done
        strPropVal = System.getProperty(STR_VAR_PROP_JAL_HOME);
        if (strPropVal != null) {
            STR_JAL_HOME = strPropVal;
            PATH_JAL_HOME = Path.of(strPropVal);
            BOL_JAL_ENV_ESTABLISH = Files.isDirectory(PATH_JAL_HOME);
            
            return BOL_JAL_ENV_ESTABLISH;
        }
        
        // Last chance - Look in the ~/.dp.env file
        //  Extract system properties
//        boolean bolWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String  strDirUser = System.getProperty("user.home");
        
        try {
            // Attempt '~/$STR_FILE_NM_DP_ENV` file open (in client home directory)
            Path            pathDpEnv = Paths.get(strDirUser, STR_FILE_NM_DP_ENV); // throws InvalidPathException
            BufferedReader  brDpEnv = Files.newBufferedReader(pathDpEnv);   // throws IOException

            // Read the .dp.env file line-by-line looking for 'DP_API_JAVA_HOME'
            String  strLine;
            while ((strLine = brDpEnv.readLine()) != null) {        // throws IOException
                
                // Ignore any comments
                if (strLine.stripLeading().startsWith("#"))
                    continue;
                
                // Search for line with STR_VAR_ENV_JAL_HOME
                if (strLine.contains(STR_VAR_ENV_JAL_HOME)) {
                    
                    // Found it - The target value is after '='
                    StringTokenizer tokens = new StringTokenizer(strLine, "=");
                    
                    tokens.nextToken();
                    String strToken = tokens.nextToken().stripLeading();
                    STR_JAL_HOME = strToken;
                    PATH_JAL_HOME = Path.of(strToken);
                    BOL_JAL_ENV_ESTABLISH = Files.isDirectory(PATH_JAL_HOME);
                }
            }

            // Close everything 
            brDpEnv.close();

        } catch (Exception e) {
            LOGGER.warn("FAILED to establish external Java API Library environment in {}/{}. Exception {} while parsing: {}",
                    strDirUser,
                    STR_FILE_NM_DP_ENV,
                    e.getClass(), 
                    e.getMessage());
        }

        // Return result
        return BOL_JAL_ENV_ESTABLISH;
    }
    
    /**
     * <p>
     * Prevent creation of <code>JalEnv</code> instances.
     * </p>
     */
    private JalEnv() {
    }

}
