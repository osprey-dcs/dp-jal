package com.ospreydcs.dp.jal.tools.appfwk;

import java.io.PrintStream;
import java.util.List;

import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.grpc.DpConnectionsConfig;
import com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser;

/**
 * <p> 
 * Record containing the network address of an available service.
 * </p>
 * <p>
 * Record contains the URL of the host service and the port address of the targeted
 * service.  Also provides operations for parsing a Java application command line 
 * for the network address (of a single service).
 * </p>
 *  
 * @param enmServ   targeted Data Platform Core Service
 * @param strUrl    server location of the target service
 * @param intPort   server port of the target service
 */
public record DpServiceAddress(DpService enmServ, String strUrl, int intPort) {
    
    //
    // Enclosed Types
    //
    
    /**
     * <p>
     * Enumeration of the Data Platform services with JAL default connections.
     * </p>
     */
    public static enum DpService {
        
        /**
         * Use Data Platform Ingestion Service default connection.
         */
        INGESTION(Default.CONNS.ingestion.channel.host.url, Default.CONNS.ingestion.channel.host.port),
        
        /**
         * Use Data Platform Query Service default connection.
         */
        QUERY(Default.CONNS.query.channel.host.url, Default.CONNS.query.channel.host.port),
        
        /**
         * Use Data Platform Annotation Service default connection.
         */
        ANNOTATION(Default.CONNS.annotation.channel.host.url, Default.CONNS.annotation.channel.host.port),
        ;
        
        /**
         * Static class required to extract JAL default connection parameters.
         */
        private static class Default {

            // JAL Library Resources
            
            /** Default connection parameters for the Data Platform services */
            private static final DpConnectionsConfig CONNS = JalConfig.getInstance().connections;
        }
        
        //
        // Constant Attributes
        //
        
        /** The service default network location */
        private final String        strUrlDef;
        
        /** The service default server port address */
        private final int           intPortDef;
        
        //
        // Constructor
        //
        
        /** Initializing constant constructor */
        private DpService(String strUrlDef, int intPortDef) {
            this.strUrlDef = strUrlDef;
            this.intPortDef = intPortDef;
        }
        
        //
        // Attribute Query
        //
        
        /** @return the default service network location */
        public String   getDefaultUrl() { return this.strUrlDef; }
        
        /** @return the default service server port address */
        public int      getDefaultPort() { return this.intPortDef; }
    }
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DpServiceAddress</code> instance with the default connection for the given service.
     * </p>
     * <p>
     * The service address is extracted from the JAL default configuration for the given <code>DpService</code> and
     * used to populate the remaining fields.
     * </p>
     * 
     * @param enmServ   targeted Data Platform Core Service
     * 
     * @return  a new <code>DpServiceAddress</code> instances with default URL and port fields for the given service
     */
    public static DpServiceAddress  from(DpService enmServ) {
        return DpServiceAddress.from(enmServ, enmServ.getDefaultUrl(), enmServ.getDefaultPort());
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DpServiceAddress</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical creator.
     * </p>
     *  
     * @param enmServ   targeted Data Platform Core Service
     * @param strUrl    server location of the target service
     * @param intPort   server port of the target service
     * 
     * @return  a new <code>DpServiceAddress</code> instances with fields given by the argument values
     */
    public static DpServiceAddress from(DpService enmServ, String strUrl, int intPort) {
        return new DpServiceAddress(enmServ, strUrl, intPort);
    }
    
    /**
     * <p>
     * Parses the given collection of application command-line arguments for a new <code>DpServiceAddress</code> and returns it.
     * </p>
     * <p>
     * The argument string array is assumed to be the command-line arguments passed to a Java <code>main(String[])</code>
     * method application entry point.  The string array is parsed for the network server URL identified by delimited variable name in 
     * class constant <code>{@link #STR_DVAR_HOST_URL}</code> and port address identified by delimited variable
     * <code>{@link #STR_DVAR_HOST_PORT}</code>.  The variable values are the string tokens following the 
     * variable values.  The values are converted into the fields of the returned record.  If a variable
     * is not present within the command line default values are provided from the JAL default configuration 
     * (see below).
     * </p>
     * <p>
     * <h2>Format</h2>
     * The arguments set identifies the Data Platform service network location with variable
     * name <code>{@link #STR_DVAR_HOST_URL}</code> = {@value #STR_DVAR_HOST_URL} and followed by the server URL.
     * The service port address is identified with variable name <code>{@link #STR_DVAR_HOST_PORT}</code> =
     * {@value #STR_DVAR_HOST_PORT} followed by the port number of targeted service.
     * <p>
     * <h2>Default Values</h2>
     * Default values are supplied from the JAL default configuration whenever <code>DpServiceAddress</code> fields
     * <code>{@link #strUrl()}</code> and/or <code>{@link #intPort()}</code> are not present in the argument.  
     * The default values are extracted from the JAL default configuration as constant values according to the
     * targeted Data Platform service.  We have the following:
     * <ul>
     * <li><code>{@link #strUrl()}</code> ({@value #STR_DVAR_HOST_URL}) = <code>{@link DpService#getDefaultUrl()}</code>.</li>
     * <li><code>{@link #intPort()}</code> ({@value #STR_DVAR_HOST_PORT}) = <code>{@link DpService#getDefaultPort()}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param enmServ   targeted Data Platform Core Service
     * @param args      collection of Java application command-line arguments
     * 
     * @return  a new <code>DpServiceAddress</code> instance obtained by parsing the given command-line arguments
     * 
     * @throws NumberFormatException    invalid port address (non-integer value)
     */
    public static DpServiceAddress    parse(DpService enmServ, String...args) throws NumberFormatException {
        DpServiceAddress  recAddr = DpServiceAddress.parseHostAddress(enmServ, args);
        
        return recAddr;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a string for displaying the parsing options for record creation.
     * </p>
     * <p>
     * This is a convenience method for displaying the command line usage for record parsing.
     * </p>
     * <p>
     * The returned string is typically used when displaying the application help response to a client invocation.
     * The variable value for {@value #STR_DVAR_HOST_URL} is "<code>URL</code>" and the variable value for
     * {@value #STR_DVAR_HOST_PORT} is "<code>port</code>".
     * </p>
     * 
     * @return  parsing description string
     */
    public static String    displayCommandLineOptions() {
        StringBuilder       buf = new StringBuilder();
        
        buf.append("[" + STR_DVAR_HOST_URL + " URL] ");
        buf.append("[" + STR_DVAR_HOST_PORT + " port] ");
        
        return buf.toString();
    }
    
    /**
     * <p>
     * Prints out a text description of the record fields to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        ps.println(strPad + "Service : " + this.enmServ);
        ps.println(strPad + "URL     : " + this.strUrl);
        ps.println(strPad + "Port    : " + this.intPort);
    }
    
    
    //
    // Record Resources
    //
    
    /** Application command-line parser */
    private static final AppArgumentsParser     PARSER = AppArgumentsParser.from();
    

    //
    // Record Constants
    //
    
    /** Argument delimited variable containing the Ingestion Service host URL */
    public static final String      STR_DVAR_HOST_URL = "--host";
    
    /** Argument delimited variable containing the Ingestion Service port number */
    public static final String      STR_DVAR_HOST_PORT = "--port"; 
    
    
    //
    // Support Methods
    //

    /**
     * <p>
     * Parse the network service location from the application command line.
     * </p>
     * 
     * @param enmServ   targeted Data Platform Core Service
     * @param args      application command-line arguments
     * 
     * @return  the service host and port location 
     * 
     * @throws NumberFormatException    the host port address had an invalid format (non-integer)
     */
    private static DpServiceAddress    parseHostAddress(DpService enmServ, String...args) throws NumberFormatException {
        
        String  strUrl;
        int     intPort;
        
        // Retrieve the host URL
        List<String>    lstUrl = PARSER.parseVariable(STR_DVAR_HOST_URL, args);
        if (lstUrl.isEmpty())
            strUrl = enmServ.getDefaultUrl();
        else
            strUrl = lstUrl.getFirst();     // only 1 URL is allowed
        
        // Retrieve the host port
        List<String>    lstPort = PARSER.parseVariable(STR_DVAR_HOST_PORT, args);
        if (lstPort.isEmpty())
            intPort = enmServ.getDefaultPort();
        else
            intPort = Integer.valueOf( lstPort.getFirst() ); // throws NumberFormatException
        
        return new DpServiceAddress(enmServ, strUrl, intPort);
    }
    
}