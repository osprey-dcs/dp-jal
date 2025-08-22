package com.ospreydcs.dp.api.query.impl;

import java.io.PrintStream;
import java.time.Duration;

import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;

/**
 * <p>
 * Record containing all the configuration parameters for a <code>QueryRequestRecoverer</code> or
 * <code>QueryRequestProcessorOld</code> object.
 * </p>
 *
 * @param bolMultistream    enable/disable gRPC multi-streaming
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
        boolean bolMultistream,
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
     * @param bolMultistream    enable/disable gRPC multi-streaming
     * @param bolCorrConcurrent correlate using concurrency (multi-threading) enabled/disabled flag
     * @param bolCorrStreaming  correlate while streaming enabled/disabled flag
     * @param cntMaxStreams     maximum number of allowable gRPC data streams allowed for request recovery
     * @param cntMaxSources     maximum number of allowable data sources per composite request
     * @param durMaxRange       maximum time range duration within composite request
     * 
     * @return  new test configuration initialized with the given arguments
     */
    public static TestConfig    from(boolean bolMultistream,
                                     boolean bolCorrConcurrent, 
                                     boolean bolCorrStreaming, 
                                     int cntMaxStreams, 
                                     int cntMaxSources, 
                                     Duration durMaxRange) 
    {
        return new TestConfig(bolMultistream, bolCorrConcurrent, bolCorrStreaming, cntMaxStreams, cntMaxSources, durMaxRange);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Configures the given processor with the current record configuration parameters.
     * </p>
     * 
     * @param prcrQuery the <code>QueryRequestRecoverer</code> to configure for data recovery
     */
    public void configure(QueryRequestRecoverer prcrQuery) {
        
        // Configure the processor
        prcrQuery.enableMultiStreaming(this.bolMultistream);
        prcrQuery.enableCorrelateConcurrency(this.bolCorrConcurrent);
        prcrQuery.enableCorrelateWhileStreaming(this.bolCorrStreaming);
        prcrQuery.setMultiStreamingMaxStreamCount(this.cntMaxStreams);
        prcrQuery.setRequestDecompMaxPvCount(this.cntMaxSources);
        prcrQuery.setRequestDecompMaxTimeRange(this.durMaxRange);
    }
    
    /**
     * <p>
     * Extracts the configuration of the given request processor and returns it as a new <code>TestConfig</code> record.
     * </p
     * 
     * @param prcrRqst  request processor under scrutiny
     * 
     * @return  the configuration of the given processor
     */
    public TestConfig extractConfiguration(QueryRequestRecoverer prcrRqst) {
        
        // Extract configuration parameters from query request processor
        final boolean   bolMultiStrm = prcrRqst.isMultiStreaming(); 
        final boolean   bolCorrCon = prcrRqst.isCorrelateConcurrencyEnabled();
        final boolean   bolCorrStrm = prcrRqst.isCorrelatingWhileStreaming();
        final int       cntMaxStrms = prcrRqst.getMultiStreamingMaxStreamCount();
        final int       cntMaxSrcs = prcrRqst.getRequestDecompMaxPvCount();
        final Duration  durMaxRng = prcrRqst.getRequestDecompMaxTimeRange();
        
        return TestConfig.from(bolMultiStrm, bolCorrCon, bolCorrStrm, cntMaxStrms, cntMaxSrcs, durMaxRng);
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