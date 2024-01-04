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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;


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
//        Yaml ymlClient = new Yaml(new Constructor(clsProps));
//        InputStream insProps = new FileInputStream(strFileYml);
//        T cfgProps = ymlClient.load(insProps);
        
        InputStream isCfg = CfgLoaderYaml.class.getClassLoader().getResourceAsStream(strFileName);
//    	InputStream	isCfg = new FileInputStream(strFileYml);
    	Struct objParams = YML_INSTANCE.loadAs(isCfg, clsStruct);

        return objParams;
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
        Struct  objParams = YML_INSTANCE.loadAs(isFile, clsStruct);
        
        return objParams;
    }
    
//    /**
//     * <p>
//     * Overrides the default property values in the <em>application.yml</em> file
//     * with any environment variables that are defined.  It is assumed the argument is
//     * the <code>AppProperties</code> instance which has already been loaded from the
//     * <em>application.yml</em> configuration file.
//     * <h2>NOTE:</h2>
//     * This is a recursive function so the method signature must be of type
//     * <code>Object</code> to function correctly.  However, the argument of initial invocation
//     * is expected to be of type <code>AppProperties</code>. 
//     * </p>
//     * 
//     * @param   <Struct>    The structure type of the accepting the YAML document
//     * 
//     * @param objProps  the <code>AppProperties</code> instance to be overridden
//     * 
//     * @return <code>true</code> if at least one parameter was overridden, <code>false</code> otherwise
//     * 
//     * @throws IllegalArgumentException encountered a null argument during recursion
//     * @throws IllegalAccessException unable to get a structure field object (java.lang.reflect.Field#get)
//     */
//    public static <Struct extends Object> boolean override(Struct objProps) throws IllegalArgumentException, IllegalAccessException {
//        boolean bolResult = false;
//        
//        if ( Objects.isNull(objProps) )
//            throw new IllegalArgumentException("Properties object is null");
//        
//        Class<?> clsProps = objProps.getClass();
//        
//        // Is the class marked for override (or recursive included class)?
////        if (! (  clsProps.isAnnotationPresent(ACfgOverride.class) 
////              || clsProps.isAnnotationPresent(ACfgOverride.Struct.class)
////              || clsProps.isAnnotationPresent(ACfgOverride.Field.class) ) 
////            ) 
////            return false;
//
//        // For every field in the class
//        for (Field fld : clsProps.getDeclaredFields()) {
//            
//            // Is field a structure containing a field to be overridden
//            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
//                fld.setAccessible(true);
//                Object objFld = fld.get(objProps);
//                
//                // Recursively override the underlying field 
//                if (CfgLoaderYaml.override(objFld))
//                    bolResult = true;
//                
//            }
//            
//            // Can the field be overridden?
//            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
//                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
//                if ( !annFld.env().isBlank() ) {
//                    String strNewVal = System.getenv(annFld.env());
//                   
//                    if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
//                        continue;
//
//                    fld.setAccessible(true);
//                    
//                    // Parse by type
//                    if (fld.getType().equals(java.lang.String.class)) {
//                        fld.set(objProps, strNewVal);
//                    
//                        bolResult = true;
//                    }
//                    if (fld.getType().equals(Boolean.class)) {
//                        Boolean bolNewVal = Boolean.parseBoolean(strNewVal);
//                        fld.set(objProps, bolNewVal);
//                        
//                        bolResult = true;
//                    }
//                    if (fld.getType().equals(Integer.class)) {
//                        Integer intNewVal = Integer.parseInt(strNewVal);
//                        fld.set(objProps, intNewVal);
//                        
//                        bolResult = true;
//                    }
//                    if (fld.getType().equals(Long.class)) {
//                        Long    lngNewVal = Long.parseLong(strNewVal);
//                        fld.set(objProps, lngNewVal);
//                        
//                        bolResult = true;
//                    }
//                    if (fld.getType().equals(Float.class)) {
//                        Float   fltNewVal = Float.parseFloat(strNewVal);
//                        fld.set(objProps, fltNewVal);
//                        
//                        bolResult = true;
//                    }
//                    if (fld.getType().equals(Double.class)) {
//                        Double  dblNewVal = Double.parseDouble(strNewVal);
//                        fld.set(objProps, dblNewVal);
//                        
//                        bolResult = true;
//                    }
//                }
//            }
//            
//        }        
//        
//        return bolResult;
//    }
//    
//    /**
//     * <p>
//     * Parses the properties in the <code>AppProperties</code> for the 
//     * <code>AOverrideCapable</code> annotations to determine the supported environment
//     * variables for the application.  The application then returns a list of these
//     * variables which it found (along with the current values, the properties object,
//     * etc. as an <code>CfgOverrideRec</code> records).
//     * It is assumed the argument is
//     * the <code>AppProperties</code> instance which has already been loaded from the
//     * <em>application.yml</em> configuration file.  
//     * <h2>NOTE:</h2>
//     * This is a recursive function so the method signature must be of type
//     * <code>Object</code> to function correctly.  However, the argument of initial invocation
//     * is expected to be of type <code>AppProperties</code>. 
//     * </p>
//     * 
//     * @param objProps  the <code>AppProperties</code> instance to be overridden
//     * 
//     * @return a list of environment variables (records) found when parsing the argument 
//     * 
//     * @throws IllegalArgumentException encountered a null argument during recursion
//     * @throws IllegAccessException unable to get a structure field object (java.lang.reflect.Field#get)
//     */
//    public static List<CfgOverrideRec> parse(Object objProps) throws IllegalArgumentException, IllegalAccessException {
//        
//        if ( Objects.isNull(objProps) )
//            throw new IllegalArgumentException("Properties object is null");
//        
//        List<CfgOverrideRec> lstVars = new LinkedList<CfgOverrideRec>();
//
//        Class<?> clsProps = objProps.getClass();
//
//        for (Field fld : clsProps.getDeclaredFields()) {
//            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
//                fld.setAccessible(true);
//                Object objFld = fld.get(objProps);
//                
//                lstVars.addAll( CfgLoaderYaml.parse(objFld) );
//            }
//            
//            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
//                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
//                if ( !annFld.env().isBlank() ) {
//                    String strVarNm  = annFld.env();
//                    String strVarVal = System.getenv(annFld.env());
//                    
//                    CfgOverrideRec var = new CfgOverrideRec(strVarNm, strVarVal, fld, objProps);
//                   
//                    lstVars.add(var);
//                }
//            }
//            
//        }        
//     
//        return lstVars;
//    }

    
    //
    // Support Methods
    //
    
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
