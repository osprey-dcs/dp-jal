package com.ospreydcs.dp.api.query.impl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Class for computing scoring and statistics of a collection of <code>ConfigResult</code> records.
 * </p>
 * <p>
 * The <code>ConfigResultScore</code> class is used for "scoring" correlator performance in various 
 * configurations.  Performance can be scored by data rate or by "hit count", the latter being the number
 * of times a specific configuration occurs in the results of a <code>ConfigResultScore</code> instance.
 * Each <code>ConfigResultScore</code> instance is used to score the performance of a correlator test run
 * using the <code>{@link #score(ConfigResult)}</code> method.
 * </p>
 * <p>
 * <h2>Usage</h2>   
 * Correlators of the types <code>QueryResponseProcessorX</code> are first configured with a 
 * <code>TestConfig</code> record, which represents the configuration under study. 
 * The correlator then recovers the data of a given <code>DpDataRequest</code> instance while the performance 
 * is monitored and recorded in a <code>ConfigResult</code> record.  Each such evaluation is scored using
 * the <code>{@link #score(ConfigResult)}</code> method which collects the results for multiple configurations.
 * </p>
 * <p>
 * <h2>Internal Operation</h2>
 * There are 2 internal types within <code>ConfigResultScore</code>:
 * <ul>
 * <li><code>{@link Index}</code> - a correlator "configuration index". </li>
 * <li><code>{@link Score}</code> - the running performance score for the correlator configuration. </li>
 * </ul>
 * These types are private and unavailable externally.
 * </p>  
 * <p> 
 * Scores are indexed by the internal record <code>Index</code> which contains a subset of the configuration
 * record <code>ConfigResult</code> fields.  The idea is to score the performance results of each processor
 * configuration.  The index configuration is then associated with a <code>Score</code> object containing
 * the "score" for that configuration.
 * </p>
 * <p>
 * Internally, the <code>ConfigResultScore</code> class
 * maintains a map of <code>Index</code> records to the <code>Score</code> instances, that is, the map
 * contains the running scores for the performance evaluations.
 * </p>
 * <h2>Correlator Configurations</h2>  
 * Currently the <code>Index</code> record maintains 3 fields: 1) the number of gRPC data streams, 
 * 2) correlation concurrently, and 3) correlate while streaming.  There are no other configuration parameters 
 * considered.
 * </p>
 * <p>
 * <s>The maximum number of gRPC data streams is currently hard-coded to the limit <code>{@link #CNT_MAX_STREAMS}</code>
 * which has value <code>{@value #CNT_MAX_STREAMS}</code>.  No correlator configurations with stream count
 * greater than that value are allowed.</s>
 * </p>
 * <p>
 * The <code>Index</code> records are now created on demand within static method 
 * <code>{@link #retrieveIndexFor(TestConfig)}</code> and stored within <code>{@link #LST_INDEXES}</code> for later
 * retrieval.  No configuration limitations are enforced.
 * </p>
 * <p>  
 * <h2>NOTES:</h2>
 * Note that we cannot simply maintain a map of (<code>TestConfig</code>, <code>Index</code>) pairs to 
 * lookup indexes against test configurations.  The Java <code>Map</code> containers generally use a 
 * lookup process based upon object values rather than field values.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 27, 2025
 *
 */
public class ConfigResultScore {


    //
    // Creators
    //

    /**
     * <p>
     * Creates a new <code>ConfigResultScore</code> instance ready for configuration scoring.
     * </p>
     * <p>
     * The supplied argument is a collection of data request used for request hit-count
     * monitoring.  All <code>DpDataRequest</code> objects within the collection will be observed
     * within the <code>{@link #score(ConfigResult)}</code> method.  The request identifier attribute is used for
     * printing out the hit counts of that request (i.e., see <code>{@link #printOutByHits(PrintStream)}</code>
     * and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   list of data request for desiring hit count monitoring
     * 
     * @return  a new <code>ConfigResultScore</code> ready for scoring and monitoring the given requests
     */
    public static ConfigResultScore from(Collection<DpDataRequest> setRqstHits) {
        return new ConfigResultScore(setRqstHits);
    }

    /**
     * <p>
     * Creates a new <code>ConfigResultScore</code> instance with configuration scoring given by the arguments.
     * </p>
     * <p>
     * This is a convenience creator which simultaneously creates the <code>ConfigResultScore</code> instance
     * and performs all the scoring on a given collection of request processor results.  It is equivalent to the
     * following invocations:
     * <ol>
     * <li><code>{@link ConfigResultScore#ConfigResultScore(List)}</code></li>
     * <li><code>{@link ConfigResultScore#score(Collection)}</code></li>
     * </ol>
     * </p>
     * <p>
     * The <code>DpDataRequest</code> collection is used for request hit-count monitoring.  
     * All <code>DpDataRequest</code> objects within that collection will be observed while scoring 
     * <code>ConfigResult</code> records within the second argument collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>{@link DpDataRequest#getRequestId()}</code> attribute is used for printing out the hit counts of 
     * that request (i.e., see <code>{@link #printOutByHits(PrintStream)}</code> 
     * and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   collection of time-series data requests desiring hit count monitoring
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     * 
     * @return  a new <code>ConfigResultScore</code> instance with scoring given by the argument
     */
    public static ConfigResultScore from(Collection<DpDataRequest> setRqstHits, Collection<ConfigResult> setResults) {
        return new ConfigResultScore(setRqstHits, setResults);
    }

    /**
     * <p>
     * Creates a new <code>ConfigResultScore</code> instance with configuration scoring given by the arguments.
     * </p>
     * <p>
     * This is a convenience creator which simultaneously creates the <code>ConfigResultScore</code> instance
     * and performs all the scoring on a given collection of request processor results.  It is equivalent to the
     * following invocations:
     * <ol>
     * <li><code>{@link ConfigResultScore#ConfigResultScore(List)}</code></li>
     * <li><code>{@link ConfigResultScore#score(Collection, double)}</code></li>
     * </ol>
     * </p>
     * <p>
     * The <code>DpDataRequest</code> collection is used for request hit-count monitoring.  
     * All <code>DpDataRequest</code> objects within that collection will be observed while scoring 
     * <code>ConfigResult</code> records within the second argument collection (i.e., those that satisfy the
     * condition <code>{@link ConfigResult#dblRate()}</code> &ge; the minimum rate).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>{@link DpDataRequest#getRequestId()}</code> attribute is used for printing out the hit counts of 
     * that request (i.e., see <code>{@link #printOutByHits(PrintStream)}</code> 
     * and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   collection of time-series data requests desiring hit count monitoring
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     * @param dblRateMin    minimum data rate for scoring
     * 
     * @return  a new <code>ConfigResultScore</code> instance with scoring given by the arguments
     */
    public static ConfigResultScore from(Collection<DpDataRequest> setRqstHits, Collection<ConfigResult> setResults, double dblRateMin) {
        return new ConfigResultScore(setRqstHits, setResults, dblRateMin);
    }


    //
    // Internal Types
    //

    /**
     * <p>
     * Record used for internal indexing of configuration result scores by processor configuration.
     * </p>
     * <p>
     * A subset of the <code>ConfigResult</code> record fields are used as a map index to the actual
     * <code>Score</code> instance. 
     * </p>
     * 
     * @param cntMaxStreams maximum number of allowable gRPC data streams allowed for request recovery
     * @param bolCorrCon    correlate using concurrency (multi-threading) enabled/disabled flag
     * @param bolCorrStrm   correlate while streaming enabled/disabled flag
     * @param enmStrmType   the gRPC stream type used for the request data recovery
     */
    private static record Index(int cntMaxStreams, boolean bolCorrCon, boolean bolCorrStrm, DpGrpcStreamType enmStrmType) {

        
        //
        // Creators
        //

        /** 
         * <p>
         * Create a new <code>Index</code> from the given parameters.
         * </p>
         * 
         * @param cntMaxStreams maximum number of allowable gRPC data streams allowed for request recovery
         * @param bolCorrCon    correlate using concurrency (multi-threading) enabled/disabled flag
         * @param bolCorrStrm   correlate while streaming enabled/disabled flag
         * @param enmStrmType   the gRPC stream type used for the request data recovery
         */
        private static Index from(int cntMaxStreams, boolean bolCorrCon, boolean bolCorrStrm, DpGrpcStreamType enmStrmType) {
            return new Index(cntMaxStreams, bolCorrCon, bolCorrStrm, enmStrmType);
        }

        /**
         * <p>
         * Create a new <code>Index</code> from the given processor configuration performance result record.
         * </p>
         * <p>
         * The returned <code>Index</code> fields are taken respectively from the argument fields
         * <ol> 
         * <li><code>{@link ConfigResult#recConfig()}.{@link TestConfig#cntMaxSources()}</code> </li>
         * <li><code>{@link ConfigResult#recConfig()}.{@link TestConfig#bolCorrConcurrent()}</code> </li>
         * <li><code>{@link ConfigResult#recConfig()}.{@link TestConfig#bolCorrStreaming()}</code> </li>
         * <li><code>{@link ConfigResult#enmStrmType()}</code> </li>
         * </ol>
         * </p> 
         * 
         * @param recCfg    correlator configuration record supplying index record fields
         * 
         * @return  new <code>Index</code> record with configuratoin parameters given by argument
         */
        private static Index from(ConfigResult recResult) {

            return new Index(recResult.recConfig().cntMaxStreams(), 
                    recResult.recConfig().bolCorrConcurrent(), 
                    recResult.recConfig().bolCorrStreaming(),
                    recResult.enmStrmType());
        }

        //            /**
        //             * <p>
        //             * Create a new <code>Index</code> from the given correlator test configuration record.
        //             * </p>
        //             * <p>
        //             * The returned <code>Index</code> fields are taken respectively from the argument fields
        //             * <ol> 
        //             * <li><code>{@link TestConfig#cntMaxSources()}</code> </li>
        //             * <li><code>{@link TestConfig#bolCorrConcurrent()}</code> <li>
        //             * <li><code>{@link TestConfig#bolCorrStreaming()}</code> </li>
        //             * </ol>
        //             * </p> 
        //             * 
        //             * @param recCfg    correlator configuration record supplying index record fields
        //             * 
        //             * @return  new <code>Index</code> record with configuratoin parameters given by argument
        //             */
        //            private static Index from(TestConfig recCfg) {
        //                return new Index(recCfg.cntMaxStreams(), recCfg.bolCorrConcurrent(), recCfg.bolCorrStreaming());
        //            }
        //            

        
        //
        // Record Constants
        //

        /** The "null" index */
        @SuppressWarnings("unused")
        private final static Index     NULL_INDEX = Index.from(0, false, false, DpGrpcStreamType.FORWARD);


        //
        // Operations
        //

        /**
         * <p>
         * Checks if given <code>ConfigResult</code> record is equivalent to this index.
         * </p>
         * 
         * @param recResult <code>ConfigResult</code> record under equivalence test
         * 
         * @return  <code>true</code> if index record fields are equal
         */
        private boolean  isIndexEquivTo(ConfigResult recResult) {
            return this.cntMaxStreams==recResult.recConfig().cntMaxStreams() &&
                    this.bolCorrCon==recResult.recConfig().bolCorrConcurrent() &&
                    this.bolCorrStrm==recResult.recConfig().bolCorrStreaming() &&
                    this.enmStrmType==recResult.enmStrmType();
        }

        /**
         * <p>
         * Checks if the given <code>Index</code> record is equivalent to this one.
         * </p>
         * 
         * @param recIndex  <code>Index</code> record under comparison
         *  
         * @return  <code>true</code> if record fields are equal
         */
        @SuppressWarnings("unused")
        private boolean  isEquiv(Index recIndex) {
            return this.cntMaxStreams==recIndex.cntMaxStreams &&
                    this.bolCorrCon==recIndex.bolCorrCon &&
                    this.bolCorrStrm==recIndex.bolCorrStrm;
        }
    }

    /**
     * <p>
     * Internal score given to <code>ConfigResult</code> records.
     * </p>
     * <p>
     * The <code>Score</code> instances maintain the "score" given to a particular processor configuration.
     * The "score" is a measure of the processor performance for a given configuration and is updated
     * using the method <code>{@link ConfigResultScore#score(ConfigResult)}</code>. 
     * The configuration for the <code>Score</code> is maintained in the <code>{@link #recIndex}</code> attribute
     * set at creation. 
     * </p>
     * <p>
     * Within a <code>ConfigResultScore</code> object <code>Score</code> instances are maintained
     * within the <code>{@link ConfigResultScore#mapIndexScore}</code> attribute.  There they are indexed
     * by the corresponding <code>Index</code> record containing the processor configuration. 
     * </p>
     * 
     * @see ConfigResultScore#score(ConfigResult)
     */
    public static class Score {

        
        //
        // Creator
        //

        /** 
         * Creates a new, empty <code>Score</code> instance for the given <code>Index</code> configuration.
         *   
         * @param recIndex  index record containing the score's processor configuration
         * 
         * @return  a new, empty <code>Score</code> instance for the given processor configuration
         */
        private static Score from(Index recIndex) {
            return new Score(recIndex);
        }


        //
        // Class Methods
        //

        /**
         * <p>
         * Comparator providing reverse ordering of <code>Score</code> objects by data rate.
         * </p>
         * <p>
         * The <code>Score</code> instances are ordered by the <code>{@link Score#dblRateAvg}</code> field
         * with the largest value occurring first.
         * </p>
         */
        private static Comparator<Score>    createRateOrdering() {
            Comparator<Score>   cmp = (o1, o2) -> {
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
         * Comparator providing reverse ordering of <code>Score</code> objects by hit count.
         * </p>
         * <p>
         * The <code>Score</code> instances are ordered by the <code>{@link Score#cntHitsTot}</code> field with
         * the largest value occurring first.
         * </p>
         */
        private static Comparator<Score>    createHitOrdering() {
            Comparator<Score>   cmp = (o1, o2) -> {
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
        // Score Resources
        //

        /** Index record for the score (back pointer) - the score is for this record configuration */
        private  final Index   recIndex;

        /** Map of (request, hits) request hit counters seen while scoring */
        private final Map<DpDataRequest, Integer>   mapRqstToHits = new HashMap<>();


        //
        // Score Attributes
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
        private  int     cntHitsTot = 0; 

        /** The average number of request decomposition components, i.e., average size of composite request list */ 
        private double  dblRqstCmpsAvg = 0;


        //
        // Constructor
        //

        /**
         * <p>
         * Constructs a new, empty <code>Score</code> instance for the given <code>Index</code>.
         * </p>
         *
         * @param recIndex  processor configuration index
         */
        private Score(Index recIndex) {
            this.recIndex = recIndex;
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
         * @return  the average size of the composite request list
         */
        public double   getRequestComponentsAvgCount() {
            return this.dblRqstCmpsAvg;
        }


        //
        // Operations
        //

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
         * @param strPad    left-side padding for parameter descriptions 
         */
        public void printOut(PrintStream ps, String strPad) {

            ps.println(strPad + "Score parameter for Index " + this.recIndex + ":");
            ps.println(strPad + "  Total number of index hits    : " + this.cntHitsTot);
            ps.println(strPad + "  Minimum data rate (MBps)      : " + this.dblRateMin);
            ps.println(strPad + "  Maximum data rate (MBps)      : " + this.dblRateMax);
            ps.println(strPad + "  Average data rate (MBps)      : " + this.dblRateAvg);
            ps.println(strPad + "  Data rate 2nd moment (MBps)^2 : " + this.dblRateSqrd);
            ps.println(strPad + "  Data rate stand. dev. (MBps)  : " + this.dataRateStd());
            ps.println(strPad + "  Average request decomp. count : " + this.dblRqstCmpsAvg);
            for (Map.Entry<DpDataRequest, Integer> entry : this.mapRqstToHits.entrySet()) {
                String  strRqstId = entry.getKey().getRequestId();
                Integer cntHits = entry.getValue();

                ps.println(strPad + "  Request " + strRqstId + " hit count :" + cntHits);
            }
        }
    }


    //
    // Class Resources
    //

    /** List of all possible indexes for the given <code>{@link #CNT_MAX_STREAMS}</code> - initialized in static section */
    private static final List<Index>    LST_INDEXES = new LinkedList<>();


    //
    // Instance Resources
    //

    /** Map of indexes to result scores */
    private final Map<Index, Score>             mapIndexScore = new HashMap<>();

    /** Map of (name, request) pairs used in data request monitoring */
    private final Collection<DpDataRequest>     setRqstHits;


    //
    // Constructor
    //

    /**
     * <p>
     * Constructs a new <code>ConfigResultScore</code> instance ready for configuration scoring.
     * </p>
     * <p>
     * The supplied argument is a collection of <code>DpDataRequest</code> used for request hit-count
     * monitoring.  All <code>DpDataRequest</code> objects within the collection will be observed
     * within the <code>{@link #score(ConfigResult)}</code> method.  The <code>{@link DpDataRequest#getRequestId()}</code>
     * attribute is used for printing out the hit counts of that request (i.e., 
     * see <code>{@link #printOutByHits(PrintStream)}</code> and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   collection of time-series data requests desiring hit count monitoring
     */
    public ConfigResultScore(Collection<DpDataRequest> lstRqstHits) {
        this.setRqstHits = lstRqstHits;
    }

    /**
     * <p>
     * Constructs a new <code>ConfigResultScore</code> instance with configuration scoring given by the arguments.
     * </p>
     * <p>
     * This is a convenience constructor which simultaneously creates the <code>ConfigResultScore</code> instance
     * and performs all the scoring on a given collection of request processor results.  It is equivalent to the
     * following invocations:
     * <ol>
     * <li><code>{@link ConfigResultScore#ConfigResultScore(List)}</code></li>
     * <li><code>{@link ConfigResultScore#score(Collection)}</code></li>
     * </ol>
     * </p>
     * <p>
     * The <code>DpDataRequest</code> collection is used for request hit-count monitoring.  
     * All <code>DpDataRequest</code> objects within that collection will be observed while scoring 
     * <code>ConfigResult</code> records within the second argument collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>{@link DpDataRequest#getRequestId()}</code> attribute is used for printing out the hit counts of 
     * that request (i.e., see <code>{@link #printOutByHits(PrintStream)}</code> 
     * and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   collection of time-series data requests desiring hit count monitoring
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     */
    public ConfigResultScore(Collection<DpDataRequest> lstRqstHits, Collection<ConfigResult> setResults) {
        this(lstRqstHits);

        this.score(setResults);
    }

    /**
     * <p>
     * Constructs a new <code>ConfigResultScore</code> instance with configuration scoring given by the arguments.
     * </p>
     * <p>
     * This is a convenience constructor which simultaneously creates the <code>ConfigResultScore</code> instance
     * and performs all the scoring on a given collection of request processor results.  It is equivalent to the
     * following invocations:
     * <ol>
     * <li><code>{@link ConfigResultScore#ConfigResultScore(List)}</code></li>
     * <li><code>{@link ConfigResultScore#score(Collection, double)}</code></li>
     * </ol>
     * </p>
     * <p>
     * The <code>DpDataRequest</code> collection is used for request hit-count monitoring.  
     * All <code>DpDataRequest</code> objects within that collection will be observed while scoring 
     * <code>ConfigResult</code> records within the second argument collection (i.e., those that satisfy the
     * condition <code>{@link ConfigResult#dblRate()}</code> &ge; the minimum rate).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>{@link DpDataRequest#getRequestId()}</code> attribute is used for printing out the hit counts of 
     * that request (i.e., see <code>{@link #printOutByHits(PrintStream)}</code> 
     * and <code>{@link #printOutByRates(PrintStream)}</code>).
     * </p>
     *
     * @param setRqstHits   collection of time-series data requests desiring hit count monitoring
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     * @param dblRateMin    minimum data rate for scoring
     */
    public ConfigResultScore(Collection<DpDataRequest> lstRqstHits, Collection<ConfigResult> setResults, double dblRateMin) {
        this(lstRqstHits);

        this.score(setResults, dblRateMin);
    }


    //
    // Operations
    //

    /**
     * <p>
     * Scores result records within argument collection that have data rates greater than or equal to 
     * the given minimum value.  
     * </p>
     * Only those result records with attribute <code>{@link ConfigResult#dblRate()}</code> &ge; 
     * the given minimum rate will be scored.  Specifically, for each result record <code>r</code>
     * within the collection we have
     * <pre>
     *   <code>r.dblRate()</code> &ge; <code>dblRateMin</code> &rArr; <code>{@link #score(r)}</code>
     * </pre>  
     * Thus, this method is essentially a convenience, deferring to <code>{@link #score(ConfigResult)}</code> 
     * for repeated invocation on each element of the argument satisfying the above condition.  
     * The returned value is the number of <code>ConfigResult</code> records that satisfied the condition
     * and were scored. 
     * </p>
     * 
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     * @param dblRateMin    minimum data rate for scoring
     * 
     * @return  the number of <code>ConfigResult</code> records that were scored (i.e., satisfied the minimum data rate)
     */
    public int  score(Collection<ConfigResult> setResults, double dblRateMin) {

        // Initialize counter and score results with valid data rates
        int     cntScores = 0;

        for (ConfigResult recResult : setResults) {

            // Check rate
            if (recResult.dblRate() >= dblRateMin) {
                this.score(recResult);
                cntScores++;
            }
        }

        return cntScores;
    }

    /**
     * <p>
     * Scores the entire collection of result records and saves the scoring results into the collection
     * of scores based upon configuration (i.e., the equivalent <code>Index</code> configuration).
     * </p>
     * <p>
     * This method is essentially a convenience, deferring to <code>{@link #score(ConfigResult)}</code> for
     * repeated invocation on each element of the argument collection.  The returned value is simply the 
     * average of all data rates scored and returned by <code>{@link #score(ConfigResult)}</code>.
     * </p>
     * 
     * @param setResults    collection of <code>ConfigResult</code> records to be scored
     * 
     * @return  new data rate average value of the collective configuration score
     */
    public double score(Collection<ConfigResult>  setResults) {

        double  dblRateAvg = setResults
                .stream()
                .mapToDouble(rec -> this.score(rec))
                .sum()
                /setResults.size();

        return dblRateAvg;
    }

    /**
     * <p>
     * Scores individual result record and saves the scoring results into the collection of
     * scoring based upon configuration (i.e., the equivalent <code>Index</code> configuration).
     * </p>
     * <p>
     * Here the running score for a configuration (by <code>Index</code>) are the date rate statistics 
     * (e.g., such as the average <code>{@link ConfigResult#dblRate}</code> field).
     * Note that scoring also includes the various hit counts for the configuration (i.e., <code>Index</code>)
     * and other <code>ConfigResult</code> parameters.
     * </p>
     * <p>
     * As a convenience the method returns the new data rate average value.  
     * </p>
     * 
     * @param recCfg    the configuration result to be scored (containing configuration record)
     * @param dblValue  value to average into the current configuration score
     * 
     * @return  new data rate average value of the configuration score
     */
    public double   score(ConfigResult recResult) {

        // Get the index for the configuration w/in the result
        Index   recIndex = ConfigResultScore.retrieveIndexFor(recResult);

        // Get the current score record for the index - make new score if none exists
        Score   score = this.mapIndexScore.get(recIndex);

        if (score == null) { 
            score = Score.from(recIndex);

            this.mapIndexScore.put(recIndex, score);
        }

        // Average in the data rate scores and increment hit counter
        score.dblRateAvg *= score.cntHitsTot;       // total the previous rate
        score.dblRateAvg += recResult.dblRate();    // add the new rate

        score.dblRateSqrd *= score.cntHitsTot;
        score.dblRateSqrd += recResult.dblRate()*recResult.dblRate();

        score.dblRqstCmpsAvg *= score.cntHitsTot;
        score.dblRqstCmpsAvg += recResult.lstCmpRqsts().size();

        score.cntHitsTot++;                         // increment hit counter
        score.dblRateAvg /= score.cntHitsTot;       // compute new avg rate
        score.dblRateSqrd /= score.cntHitsTot;      // compute new 2nd moment
        score.dblRqstCmpsAvg /= score.cntHitsTot;   // compute the avg request decomposition count

        // Initialize min/max rates
        if (score.cntHitsTot == 1) {
            score.dblRateMax = recResult.dblRate();
            score.dblRateMin = recResult.dblRate();

        } else {

            // Update min/max values
            if (recResult.dblRate() > score.dblRateMax)
                score.dblRateMax = recResult.dblRate();
            if (recResult.dblRate() < score.dblRateMin)
                score.dblRateMin = recResult.dblRate();
        }

        // Record hit count for particular data requests
        for (DpDataRequest rqst : this.setRqstHits) {
            if (recResult.request().equals(rqst)) {
                Integer intCnt = score.mapRqstToHits.get(rqst);

                if (intCnt == null) {
                    Integer intFirst = 1;
                    score.mapRqstToHits.put(rqst, intFirst);

                } else {
                    Integer intNewCnt = ++intCnt;
                    score.mapRqstToHits.put(rqst, intNewCnt);
                }
            }
        }

        return score.dblRateAvg;
    }


    //
    // State Inquiry
    //

    /**
     * <p>
     * Gets the hit count <code>Score</code> field for the given configuration results.
     * </p>
     * 
     * @param recResult    result record
     * 
     * @return  number of scoring instances with this configuration or 0 if none
     */
    public int  getConfigurationCount(ConfigResult recResult) {

        // Get the index for the configuration w/in the result
        Index   recIndex = ConfigResultScore.retrieveIndexFor(recResult);

        // Get the current score record for the index - make new score if none exists
        Score   recScore = this.mapIndexScore.get(recIndex);

        if (recScore == null)
            return 0;

        return recScore.cntHitsTot;
    }

    /**
     * <p>
     * Finds and returns the score with the largest average data rate.
     * </p>
     * 
     * @return  <code>Score</code> record with largest data rate, or <code>null</code> if none exists
     */
    public ConfigResultScore.Score  getScoreWithBestAvgRate() {

        // Find the score with largest average rate
        Optional<Score> optScore = this.mapIndexScore
                .values()
                .stream()
                .reduce((s1,s2) -> {
                    if (s1.dblRateAvg > s2.dblRateAvg)
                        return s1;
                    else 
                        return s2;
                }
                        );

        // Check if score exists and return it
        if (optScore.isEmpty())
            return null;
        else
            return optScore.get();
    }

    /**
     * <p>
     * Gets the score for the given configuration result
     * </p>
     *  
     * @param recResult    result record
     * 
     * @return  current configuration score, or <code>null</code> if none
     */
    public ConfigResultScore.Score   getScore(ConfigResult recResult) {
        // Get the index for the configuration w/in the result
        Index   recIndex = ConfigResultScore.retrieveIndexFor(recResult);

        // Get the current score record for the index - make new score if none exists
        Score   recScore = this.mapIndexScore.get(recIndex);

        return recScore;
    }

    /**
     * <p>
     * Returns the set of all <code>Score</code> records ordered by configuration hit count, large to small.
     * </p>
     * 
     * @return  all the <code>ConfigResult</code> score records so far
     */
    public SortedSet<Score> recoverScoresByHits() {

        SortedSet<Score>    setScores = new TreeSet<>(Score.createHitOrdering());

        //            this.mapScore.forEach((k,v) -> setScores.add(v));
        for (Score score : this.mapIndexScore.values()) {
            setScores.add(score);
        }

        return setScores;
    }

    /**
     * <p>
     * Returns the set of all <code>Score</code> records ordered by data rate, large to small.
     * </p>
     * 
     * @return  all the <code>ConfigResult</code> score records so far
     */
    public SortedSet<Score> recoverScoresByRates() {
        SortedSet<Score>    setScores = new TreeSet<>(Score.createRateOrdering());

        //            this.mapScore.forEach((k,v) -> setScores.add(v));
        for (Score score : this.mapIndexScore.values()) {
            setScores.add(score);
        }

        return setScores;
    }

    /**
     * <p>
     * Prints out the given configuration results scoring.
     * </p>
     * 
     * @param ps    print stream to receive results
     */
    public void printOutByHits(PrintStream ps) {
        SortedSet<Score>    setScores = this.recoverScoresByHits();

        ps.println("ConfigResult Scoring by Hit Count");
        printOut(ps, setScores);
    }

    /**
     * <p>
     * Prints out the given configuration results scoring.
     * </p>
     * 
     * @param ps    print stream to receive results
     */
    public void printOutByRates(PrintStream ps) {
        SortedSet<Score>    setScores = this.recoverScoresByRates();

        ps.println("ConfigResult Scoring by Data Rates");
        printOut(ps, setScores);
    }


    // 
    // Support Methods
    //

    /**
     * <p>
     * Returns the internal index corresponding to the given <code>ConfigResult</code record.
     * </p>
     * <p>
     * The static list of index records, <code>{@link #LST_INDEXES}</code> is searched for an
     * index equivalent to the given test configuration.  If the list currently contains the
     * equivalent index it is returned.  Otherwise, a new index is created, added to the list,
     * and returned.
     * </p> 
     * 
     * @param recResult    processor configuration performance result record to cross reference
     * 
     * @return  equivalent configuration <code>Index</code> record for the given argument
     */
    private static Index    retrieveIndexFor(ConfigResult recResult) {

        // Check if there already is an equivalent configuration index in list
        for (Index index : LST_INDEXES) {
            if (index.isIndexEquivTo(recResult))
                return index;
        }

        // No indexes for the given test configuration
        Index   index = Index.from(recResult);
        LST_INDEXES.add(index);

        return index;
    }

    /**
     * <p>
     * Prints out a textual description of the ordered scores to the given output.
     * </p>
     * 
     * @param ps        output for the text description
     * @param setScores ordered set of configuration scores to print out
     */
    private static void printOut(PrintStream ps, SortedSet<Score> setScores) {

        // Compute collective data
        final int       cntScores = setScores.size();
        final int       cntHitsTot = setScores.stream().mapToInt(score -> score.cntHitsTot).sum();
        final double    dblRateAvg = setScores.stream().mapToDouble(score -> score.dblRateAvg).sum()/cntScores;
        final double    dblRateStd = setScores.stream().mapToDouble(score -> score.dataRateStd()).sum()/cntScores;

        ps.println("  Total number of results = " + cntScores);
        ps.println("  Total number of hits    = " + cntHitsTot);
        ps.println("  Data rate average       = " + dblRateAvg);
        ps.println("  Data rate standard dev. = " + dblRateStd);
        for (Score score : setScores) {

            // Print configuration 
            ps.print("  Config=[Max streams=" + score.recIndex.cntMaxStreams);
            ps.print(", correl conc=" + score.recIndex.bolCorrCon);
            ps.print(", correl stream=" + score.recIndex.bolCorrStrm);
            ps.print(", stream type=" + score.recIndex.enmStrmType);
            ps.print("]");
            ps.println();

            // Print rate statistics
            ps.println("    Rate Avg=" + score.dblRateAvg 
                    + ", Rate Min=" + score.dblRateMin 
                    + ", Rate Max=" + score.dblRateMax
                    + ", Rate Std=" + score.dataRateStd());

            // Print hit counts
            ps.print("    Hits Total=" + score.cntHitsTot);
            for (Map.Entry<DpDataRequest, Integer> entry : score.mapRqstToHits.entrySet()) {
                String  strRqstNm = entry.getKey().getRequestId();
                Integer cntHits = entry.getValue();

                ps.print(", " + strRqstNm + " Hits=" + cntHits);
            }
            ps.println();

            // Print average size of component request list
            ps.println("    Avg request component list size=" + score.dblRqstCmpsAvg);
        }
    }
}