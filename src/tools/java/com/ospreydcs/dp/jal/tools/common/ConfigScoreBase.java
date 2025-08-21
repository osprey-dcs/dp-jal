package com.ospreydcs.dp.jal.tools.common;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.jal.tools.query.assem.AggrAssemblyTestConfig;
import com.ospreydcs.dp.jal.tools.query.assem.QueryAssemblyTestResult;

/**
 * <p>
 * Internal score given to <code>Config</code> records.
 * </p>
 * <p>
 * The <code>ConfigScoreBase</code> instances maintain the "score" given to a particular test configuration.
 * The "score" is a measure of the test performance for a given configuration and is updated
 * using the method <code>{@link ConfigScorerBase#score(Result)}</code>. 
 * The configuration for the <code>ConfigScoreBase</code> is maintained in the <code>{@link #recCfg}</code> attribute
 * set at creation. 
 * </p>
 * <p>
 * Within a <code>ConfigScorerBase</code> object, <code>ConfigScoreBase</code> instances are maintained
 * within the <code>{@link ConfigScorerBase#mapIndexScore}</code> attribute.  There they are indexed
 * by the corresponding <code>Config</code> record containing the assembly configuration. 
 * </p>
 * <p>
 * <h2><code>Config</code> Record Types</h2>
 * The <code>Config</code> record type must override the <code>{@link Record#equals(Object)}</code> method
 * to check for configuration <em>equivalence</em>.  Two record are <em>equivalent</em> if all configuration
 * field values are equal.
 * </p>
 * <p>
 * The above requirement is looser than strict equality which is determined in the base class implementation
 * <code>{@link Record#equals(Object)}</code>, where two records are <em>equal</em>.  That is, the base
 * class implementation checks for strict equality as <em>objects</em>, <b>not</b> record field values.
 * </p>
 * <p>
 * <h2><code>Result</code> Record Types</h2>
 * The <code>Result</code> record must contain references to the original test case conditions for which the
 * result was generated.  Generally a <code>Result</code> record maintains a field containing the original
 * test case record which generated the result.  The <code>Score</code> base class <code>{@link ConfigScoreBase}</code>
 * has multiple abstract method requirements which extract various properties from <code>Result</code> records.
 * </p>   
 * 
 * @see ConfigScorerBase#score(Result)
 * 
 * @param <Config>  Configuration record for test case evaluations (with <code>{@link Record#equals(Object)}</code> override)
 * @param <Result>  The test result for a given test case (must contain the configuration information)
 */
public abstract class ConfigScoreBase<Config extends Record, Result extends Record> {


    //
    // Creator
    //

    //        /** 
    //         * Creates a new, empty <code>ConfigScoreBase</code> instance for the given <code>AggAssemblyTestConfig</code> configuration.
    //         *   
    //         * @param recCfg  index record containing the score's processor configuration
    //         * 
    //         * @return  a new, empty <code>ConfigScoreBase</code> instance for the given processor configuration
    //         */
    //        private static <Config extends Record, Result extends Record> ConfigScoreBase<Config, Result> from(Config recIndex) {
    //            return new ConfigScoreBase<Config, Result>(recIndex);
    //        }

    //
    // Abstract Methods
    //

    /**
     * <p>
     * Extracts the test configuration for the test result from the given result record.
     * </p>
     * 
     * @param recResult a <code>Result</code> test record containing the configurations for the test
     * 
     * @return  the test case configuration for the given test result
     */
    protected abstract Config   extractConfiguration(Result recResult);

    /**
     * <p>
     * Extracts the test result success status (i.e. success or failure) from the given result record.
     * </p>
     * 
     * @param recResult a <code>Result</code> test result record containing the result status 
     * 
     * @return  <code>true</code> if the test succeeded, <code>false</code> if the test failed
     */
    protected abstract boolean  isSuccess(Result recResult);

    /**
     * <p>
     * Extracts the originating <code>DpDataRequest</code> for the test case from the given result record.
     * </p>
     * 
     * @param recResult a <code>Result</code> test result record containing the time-series data request used
     * 
     * @return  the time-series data request used for the test case
     */
    protected abstract DpDataRequest    extractDataRequest(Result recResult);

    /**
     * <p>
     * Extracts the target data rate from the given result record.
     * </p>
     * <p>
     * The returned value is used to compute the scoring for the given result.  Note that the 
     * configuration information must also be available.
     * </p>
     * 
     * @param recResult a <code>Result</code> test result record containing data rate (and other results) 
     * 
     * @return  the data rate for the test result (in MBps)
     */
    protected abstract double    extractDataRate(Result recResult);
    
    /**
     * <p>
     * Prints out a text description of the given configuration to the given output sink.
     * </p>
     * <p>
     * The parameter values of the given configuration are written to the given output.  The 
     * additional (optional) argument is used to pad the headers of each parameter (typically
     * a sequence of white spaces). This is convenient when the score output is part of a larger output set.
     * </p>
     * 
     * @param ps        output stream to receive the score parameter description
     * @param strPad    left-side padding for parameter descriptions (or <code>null</code>)
     * @param recConfig the configuration to be written out 
     */
    protected abstract void     printOutConfiguration(PrintStream ps, String strPad, Config recConfig);


    //
    // Class Tools
    //

    /**
     * <p>
     * Comparator providing reverse ordering of <code>ConfigScoreBase</code> objects by data rate.
     * </p>
     * <p>
     * The <code>ConfigScoreBase</code> instances are ordered by the <code>{@link ConfigScoreBase#dblRateAvg}</code> field
     * with the largest value occurring first.
     * </p>
     */
    public static <Config extends Record, Result extends Record> Comparator<ConfigScoreBase<Config, Result>>    createRateOrdering() {
        Comparator<ConfigScoreBase<Config, Result>>   cmp = (o1, o2) -> {
            if (o1.dblRateAvg > o2.dblRateAvg)
                return -1;
            // Must avoid collisions - clobbers an existing score
            //                    else if (o1.dblScore == o2.dblScore)
            //                        return 0;
            else
                return +1;
        };

        return cmp;
    }

    /**
     * <p>
     * Comparator providing reverse ordering of <code>ConfigScoreBase</code> objects by hit count.
     * </p>
     * <p>
     * The <code>ConfigScoreBase</code> instances are ordered by the <code>{@link ConfigScoreBase#cntHitsTot}</code> field with
     * the largest value occurring first.
     * </p>
     */
    public static <Config extends Record, Result extends Record> Comparator<ConfigScoreBase<Config, Result>>    createHitOrdering() {
        Comparator<ConfigScoreBase<Config, Result>>   cmp = (o1, o2) -> {
            if (o1.cntHitsTot > o2.cntHitsTot)
                return -1;
            // Must avoid collisions - clobbers an existing score
            //                    else if (o1.cntHits == o2.cntHits)
            //                        return 0;
            else 
                return +1;
        };

        return cmp;
    }


    //
    // Defining Attributes
    //

    /** AggAssemblyTestConfig record for the score (back pointer) - the score is for this record configuration */
    private final Config       recCfg;


    //
    // Instance Resources
    //

    /** Map of (request, hits) request hit counters seen while scoring */
    private final Map<DpDataRequest, Integer>   mapRqstToHits = new HashMap<>();


    //
    // State Variables
    //

    /** Average data rate of this configuration */
    private  double  dblRateAvg = 0.0;

    /** Minimum data rate seen for this configuration */
    private  double  dblRateMin = 0.0;

    /** Maximum data rate seen for this configuration */
    private  double  dblRateMax = 0.0;

    /** Data rate 2nd order moment (used in standard deviation calculation) */
    private double  dblRateSqrd = 0.0;

    /** The number of hits for this score (configuration index) */
    private  int    cntHitsTot = 0; 


    /** The number of test failures for the score test configuration */
    private int     cntTestFailures = 0;


    //
    // Constructor
    //

    /**
     * <p>
     * Constructs a new, empty <code>ConfigScoreBase</code> instance for the given <code>AggAssemblyTestConfig</code>.
     * </p>
     *
     * @param recCfg  processor configuration index
     */
    protected ConfigScoreBase(Config recCfg) {
        this.recCfg = recCfg;
    }

    //
    // Getters
    //

    /**
     * @return  the minimum data rate seen for the scoring
     */
    public double   getDataRateMin() {
        return this.dblRateMin;
    }

    /**
     * @return  the maximum data rate seen for the scoring
     */
    public double   getDataRateMax() {
        return this.dblRateMax;
    }

    /**
     * @return  the data rate average for the score
     */
    public double   getDataRateAvg() {
        return this.dblRateAvg;
    }

    /**
     * @return  the number of hit counts for the score configuration
     */
    public int  getHitCount() {
        return this.cntHitsTot;
    }

    /**
     * @return  the number of test failures for the score configuration
     */
    public int  getTestFailures() {
        return this.cntTestFailures;
    }

    /**
     * <p>
     * Computes and returns the data rate standard deviation for the current score.
     * </p>
     * <p>
     * The standard deviation &sigma; for the data rate is given by the formula
     * <pre>
     *   &sigma; = [&lt;<i>r</i><sup>2</sup>&gt; - <i>r</i><sub>avg</sub><sup>2</sup>]<sup>1/2</sup>
     * </pre>
     * where &lt;<i>r<sup>2</sup></i>&gt; = <code>{@link #dblRateSqrd}</code> is the second moment of
     * the data rate distribution and <i>r</i><sub>avg</sub> = <code>{@link #dblRateAvg}</code> is the
     * first moment of the data rate distribution.
     * </p>
     * 
     * @return  the standard deviation of the data rates for the current score
     */
    public double  dataRateStd() {
        double  dblStd = Math.sqrt( this.dblRateSqrd - this.dblRateAvg*this.dblRateAvg );

        return dblStd;
    }


    //
    // Operations
    //

    /**
     * <p>
     * Adds in the given test result to the current running score.
     * </p>
     * <p>
     * The given <code>Result</code> test result field values are used to 
     * update the running score applying to the <code>ConfigScoreBase</code> instance's configuration.
     * The value of the current data rate average value is returned as a convenience.
     * </p>
     * <p>
     * If the <code>{@link Result#recTestStatus()}</code> field indicates
     * a test failure the attribute <code>{@link #cntTestFailures}</code> is incremented
     * and the test result record is ignored (i.e., the method returns immediately).
     * </p>
     * 
     * @apiNote
     * It is assumed that the given <code>QueryAssemblyTestResult</code> applies to this
     * <code>ConfigScoreBase</code> instance. Specifically, the configuration extracted by
     * <code>{@link AggrAssemblyTestConfig#from(QueryAssemblyTestResult)}</code> must
     * be equivalent to the <code>{@link #recCfg}</code> record attribute of this instance.
     * Otherwise an exception is thrown.
     *  
     * @param recResult the test results containing the sampled aggregate assembly results
     * 
     * @return  the current running average data rate
     * 
     * @throws  IllegalArgumentException    the argument does not apply to this <code>ConfigScoreBase</code> instance
     */
    public double   addInResult(Result recResult) throws IllegalArgumentException {

        // Check argument
        Config  recResultCfg = this.extractConfiguration(recResult);

        if (!this.recCfg.equals(recResultCfg)) {
            throw new IllegalArgumentException(
                    JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument does NOT apply to this ConfigScoreBase instance.");
        }

        // Check if this is a test failure
        if (!this.isSuccess(recResult)) {
            this.cntTestFailures++;

            return this.dblRateAvg;
        }

        // Get the data rate within the argument
        double  dblDataRate = this.extractDataRate(recResult);

        // Initialize min/max data rates if this is the first call
        if (this.cntHitsTot == 0) {
            this.dblRateMax = dblDataRate;
            this.dblRateMin = dblDataRate;
        } 

        // Add in the data rate scores to the running averages 
        this.dblRateAvg *= this.cntHitsTot;     // un-normalize
        this.dblRateAvg += dblDataRate;         // add the new rate

        this.dblRateSqrd *= this.cntHitsTot;
        this.dblRateSqrd += dblDataRate*dblDataRate;

        // Increment the hit counter then compute averages
        this.cntHitsTot++;                         // increment hit counter

        this.dblRateAvg /= this.cntHitsTot;       // compute new avg rate
        this.dblRateSqrd /= this.cntHitsTot;      // compute new 2nd moment

        // Update min/max data rate values
        if (dblDataRate > this.dblRateMax)
            this.dblRateMax = dblDataRate;
        if (dblDataRate < this.dblRateMin)
            this.dblRateMin = dblDataRate;

        // Get the data request and update the hit count for that request
        DpDataRequest   rqst = this.extractDataRequest(recResult);

        if (this.mapRqstToHits.containsKey(rqst)) {
            Integer     intCnt = this.mapRqstToHits.get(rqst);
            Integer     intNewCnt = intCnt + 1;

            this.mapRqstToHits.put(rqst, intNewCnt);

        } else {
            Integer     intFirst = 1;

            this.mapRqstToHits.put(rqst, intFirst);
        }

        // Return the running average data rate
        return this.dblRateAvg;
    }

    /**
     * <p>
     * Prints out a text description of the current score to the given output sink.
     * </p>
     * <p>
     * The parameter values of the current score are written to the given output.  The 
     * additional (optional) argument is used to pad the headers of each parameter (typically
     * a sequence of white spaces). This is convenient when the score output is part of a larger output set.
     * </p>
     * 
     * @param ps        output stream to receive the score parameter description
     * @param strPad    left-side padding for parameter descriptions (or <code>null</code>) 
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
                    String  strPadd = strPad + "  ";

        ps.println(strPad + "Configuration " + this.recCfg.getClass().getSimpleName());
        this.printOutConfiguration(ps, strPadd, this.recCfg);
        
        ps.println(strPad + "Scoring Parameters");
        ps.println(strPadd + "Total number of cfg.  hits     : " + this.cntHitsTot);
        ps.println(strPadd + "Number test failures for cfg.  : " + this.cntTestFailures);
        ps.println(strPadd + "Minimum data rate (MBps)       : " + this.dblRateMin);
        ps.println(strPadd + "Maximum data rate (MBps)       : " + this.dblRateMax);
        ps.println(strPadd + "Average data rate (MBps)       : " + this.dblRateAvg);
        ps.println(strPadd + "Data rate 2nd moment (MBps)^2  : " + this.dblRateSqrd);
        ps.println(strPadd + "Data rate stand. dev. (MBps)   : " + this.dataRateStd());

        ps.println(strPad + "Data Request Hit Counts");
        for (Map.Entry<DpDataRequest, Integer> entry : this.mapRqstToHits.entrySet()) {
            String  strRqstId = entry.getKey().getRequestId();
            Integer cntHits = entry.getValue();

            ps.println(strPadd + "Request " + strRqstId + " hit count : " + cntHits);
        }
    }
}