package com.ospreydcs.dp.api.query.impl;

import java.io.PrintStream;
import java.time.Duration;

/**
 * <p>
 * Record containing all the configuration parameters for a <code>QueryRequestProcessorNew</code> or
 * <code>QueryRequestProcessorOld</code> object.
 * </p>
 *
 * @param bolCorrConcurrent correlate using concurrency (multi-threading) enabled/disabled flag
 * @param bolCorrStreaming  correlate while streaming enabled/disabled flag
 * @param cntMaxStreams     maximum number of allowable gRPC data streams allowed for request recovery
 * @param cntMaxSources     maximum number of allowable data sources per composite request
 * @param durMaxRange       maximum time range duration within composite request
 *
 * @author Christopher K. Allen
 * @since Jan 24, 2025
 *
 */
public record    TestConfig(
        boolean bolCorrConcurrent, 
        boolean bolCorrStreaming, 
        int cntMaxStreams, 
        int cntMaxSources, 
        Duration durMaxRange) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Create a new, initialized <code>TestConfig</code> record according to the given arguments.
     * </p>
     * 
     * @param bolCorrConcurrent correlate using concurrency (multi-threading) enabled/disabled flag
     * @param bolCorrStreaming  correlate while streaming enabled/disabled flag
     * @param cntMaxStreams     maximum number of allowable gRPC data streams allowed for request recovery
     * @param cntMaxSources     maximum number of allowable data sources per composite request
     * @param durMaxRange       maximum time range duration within composite request
     * 
     * @return  new test configuration initialized with the given arguments
     */
    public static TestConfig    from(boolean bolCorrConcurrent, boolean bolCorrStreaming, int cntMaxStreams, int cntMaxSources, Duration durMaxRange) {
        return new TestConfig(bolCorrConcurrent, bolCorrStreaming, cntMaxStreams, cntMaxSources, durMaxRange);
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Configures the given processor with the current record configuration parameters.
     * </p>
     * 
     * @param prcrQuery the <code>QueryRequestProcessorNew</code> to configure for data recovery
     */
    public void configure(QueryRequestProcessorNew prcrQuery) {
        
        // Configure the processor
        prcrQuery.enableCorrelateConcurrently(this.bolCorrConcurrent);
        prcrQuery.enableCorrelateWhileStreaming(this.bolCorrStreaming);
        prcrQuery.setMaxStreamCount(this.cntMaxStreams);
        prcrQuery.setMaxDataSourceCount(this.cntMaxSources);
        prcrQuery.setMaxTimeRange(this.durMaxRange);
    }
    
    /**
     * <p>
     * Configures the given processor with the current record configuration parameters.
     * </p>
     * 
     * @param prcrQuery the <code>QueryRequestProcessorOld</code> to configure for data recovery
     */
    @SuppressWarnings("deprecation")
    public void configure(QueryRequestProcessorOld prcrQuery) {
        
        // Configure the processor
        prcrQuery.enableCorrelateConcurrently(this.bolCorrConcurrent);
        prcrQuery.enableCorrelateWhileStreaming(this.bolCorrStreaming);
        prcrQuery.setMaxStreamCount(this.cntMaxStreams);
        prcrQuery.setMaxDataSourceCount(this.cntMaxSources);
        prcrQuery.setMaxTimeRange(this.durMaxRange);
    }
    
    /**
     * <p>
     * Prints out the configuration text description to the given print stream.
     * </p>
     * 
     * @param ps        print stream for configuration information 
     * @param strPad    line padding for parameters
     */
    public void printOut(PrintStream ps, String strPad) {
        ps.println(strPad + "  Correlate w/ concurrency : " + this.bolCorrConcurrent);
        ps.println(strPad + "  Correlate w/ streaming   : " + this.bolCorrStreaming);
        ps.println(strPad + "  Maximum stream count     : " + this.cntMaxStreams);
        ps.println(strPad + "  Maximum source count     : " + this.cntMaxSources);
        ps.println(strPad + "  Maximum range duration   : " + this.durMaxRange);
    }
}