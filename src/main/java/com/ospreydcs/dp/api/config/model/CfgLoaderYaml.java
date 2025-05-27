/*
 * Project: dp-api-common
 * File:	CfgLoaderYaml.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	PropertiesLoader
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.ospreydcs.dp.api.util.JalEnv;
import com.ospreydcs.dp.api.util.JavaRuntime;


/**
 * <p>
 * Utility Class for Loading YAML Configuration Files into Structure Classes
 * </p>
 * <p>
 * The <em>SnakeYaml</em> utility is used to parse YAML files and create new 
 * structure class instances containing the parameters specified in the file.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 * @version	2.0
 *
 */
public final class CfgLoaderYaml {

    //
    // Application Resources
    //
    
    /** Java API Library configuration file directory (relative to installation location) */
    public static final String      STR_LIB_DIR_CFG = "config";
    
    
    //
    // Class Resources
    //
    
    /** The static logging utility */
    private static final Logger     LOGGER = LogManager.getLogger();

	/** The singleton instance of the Yaml class */
	private final static Yaml		YML_INSTANCE = new Yaml();
    
    
    /**
     * <p>
     * Loads (configuration) parameters from the given input YAML file and loads them into
     * a structure class supporting the YAML file format.
     * </p>
     * <p>
     * This method accepts the file name as the argument.  It uses the Java class loader to search
     * standard resources locations for the file so no path element should be used (maintains
     * portability). 
     * </p>
     * <p>
     * If an error occurs during YAML parsing either an error message is sent to a log file if
     * the properties file cannot be found, or <em>SnakeYaml</em> sends
     * error messages to <code>sterr</code> then crashes.
     * </p>
     * 
     * @param   <Struct>    type of the structure class support the YAML document format
     * 
     * @param   strFileName file name of YAML document containing configuration parameters
     * @param   clsStruct   class type of the structure class to be created
     * 
     * @return  a new <code>Struct</code> instance containing the contents of the YAML file 
     * 
     * @throws FileNotFoundException the given file could not be found
     * @throws SecurityException the file could not be read - bad access
     */
    public static <Struct extends Object> Struct load(String strFileName, Class<Struct> clsStruct) throws FileNotFoundException, SecurityException {
        
        
        // SnakeYaml version 1.x.x
//        Yaml ymlClient = new Yaml(new Constructor(clsStruct));
//        InputStream insProps = new FileInputStream(strFileName);
//        T cfgProps = ymlClient.load(insProps);
        
        File    fileCfg = locateConfigFile(strFileName);  
        Struct  strcCfg = load(fileCfg, clsStruct);
        
        return strcCfg;
    }
    
    /**
     * <p>
     * Loads (configuration) parameters from the given input YAML file and loads them into
     * a structure class supporting the YAML file format.
     * </p>
     * <p>
     * If an error occurs during YAML parsing either an error message is sent to a log file if
     * the properties file cannot be found, or <em>SnakeYaml</em> sends
     * error messages to <code>sterr</code> then crashes.
     * </p>
     * 
     * @param   <Struct>    type of the structure class support the YAML document format
     *
     * @param   fileYaml    file instance identifying the target YAML document
     * @param   clsStruct   class type of the structure class to be created
     * 
     * @return  a new <code>Struct</code> instance containing the contents of the YAML file
     *  
     * @throws FileNotFoundException the given file does not exist
     * @throws SecurityException the file could not be read - bad access
     */
    public static <Struct extends Object> Struct load(File fileYaml, Class<Struct> clsStruct) throws FileNotFoundException, SecurityException {

        InputStream isFile = new FileInputStream(fileYaml);
        Struct      cfgParams = YML_INSTANCE.loadAs(isFile, clsStruct);
        
        return cfgParams;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Locates the configuration file with the given name on the local platform.
     * </p>
     * <p>
     * The method first attempts "dynamic configuration" by looking in the installation
     * location for the Java API Library defined by the 'DP_API_JAVA_HOME' environment
     * variable. This environment variable should be set to the installation directory of the
     * Java API Library.    
     * </p>
     * 
     * @param strFileName   the name of the configuration file
     * 
     * @return  a file object connected to the configuration file with the given name
     * 
     * @throws FileNotFoundException    the configuration file was not reachable
     */
    private static File locateConfigFile(String strFileName) throws FileNotFoundException {
        
        // Look for file in the JAL Home directory 
        try {
            File    fileCfg = JalEnv.getJalHomeFile(STR_LIB_DIR_CFG, strFileName);
            if (fileCfg.exists()) {
                LOGGER.info("Configuration taken from JAL Home file {}.", fileCfg);

                return  fileCfg;
            }
            
            LOGGER.warn("Configuration file {} not found.  Attempting internal (JAR) configuration.", fileCfg);
            
        } catch (Exception e) {
            LOGGER.warn("Java API Library environment exception {}: {}.  Attempting internal (JAR) configuration.", e.getClass(), e.getMessage());
            
        }
        
        // Look for file in JAL JAR - Try finding file with class loader
        String  strErrMsg = JavaRuntime.getQualifiedMethodNameSimple()
                + " - no resource with file name " + strFileName
                + " was found in class loader proximity.";
        
        try {
            URL     urlFile = CfgLoaderYaml.class.getClassLoader().getResource(strFileName);
            Path    pathFile = Path.of(urlFile.toURI());
            File    fileCfg = pathFile.toFile(); // throws UnsupportedOperationException
            
            if (fileCfg.canRead()) {
                LOGGER.info("Loading configuration {} from internal JAL JAR.", strFileName);
                return fileCfg;
            }
            
            else {
                strErrMsg = strErrMsg + " File " + fileCfg + " was unreadable.";
                
                throw new FileNotFoundException(strErrMsg);
            }

        } catch (Exception e) {
            strErrMsg = strErrMsg + " Exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            LOGGER.error(strErrMsg);

            throw new FileNotFoundException(strErrMsg);
        }
    }
        
    /**
     * <p>
     * Prevents construction of <code>CfgLoaderYaml</code> instances.
     * </p>
     *
     */
    private CfgLoaderYaml() {
        super();
    }

}
