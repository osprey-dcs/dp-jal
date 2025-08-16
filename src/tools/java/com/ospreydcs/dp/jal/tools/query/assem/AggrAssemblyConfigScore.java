/*
 * Project: dp-api-common
 * File:	AggrAssemblyConfigScore.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	AggrAssemblyConfigScore
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
 * @since Aug 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

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

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Class used for scoring the configuration of <code>QueryResponseAssembler</code> processors in the assembly
 * of <code>SampledAggregate</code> instances.
 * </p>
 * <p>
 * Use the <code>score(...)</code> methods to add <code>QueryAssemblyTestResult</code> records generated from
 * <code>{@link QueryAssemblyTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
 * test evaluations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 15, 2025
 *
 */
public class AggrAssemblyConfigScore {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>AggrAssemblyConfigScore</code> instance ready for test configuration scoring.
     * </p>
     * <p>
     * Creates and returns an empty <code>AggrAssemblyConfigScore</code> instance ready for scoring the
     * <code>SampledAggregate</code> assembly test results against the configuration of the 
     * <code>QueryResponseAssembler</code> processor used to create the aggregates.
     * </p>
     * <p>
     * Use the <code>score(...)</code> methods to add <code>QueryAssemblyTestResult</code> records generated from
     * <code>{@link QueryAssemblyTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
     * test evaluations.
     * </p>
     * 
     * @return  a new <code>AggrAssemblyConfigScore</code> instance ready for use
     */
    public static AggrAssemblyConfigScore   create() {
        return new AggrAssemblyConfigScore();
    }
    
    /**
     * <p>
     * Creates a new, populated <code>AggrAssemblyConfigScore</code> instance.
     * </p>
     * <p>
     * This is a convenience creator which is a combination of the following operations:
     * <ol>
     * <li><code>{@link #create()}</code>
     * <li><code>{@link #score(Collection)}</code>
     * </ol>
     * Thus, the returned object contains all the scoring information for the given argument collection.
     * </p>
     * 
     * @param setResults    the collection of test results to be scored
     * 
     * @return  a new <code>AggrAssemblyConfigScore</code> instance which scores the given argument collection
     */
    public static AggrAssemblyConfigScore   from(Collection<QueryAssemblyTestResult> setResults) {
        AggrAssemblyConfigScore score = AggrAssemblyConfigScore.create();
        
        score.score(setResults);
        
        return score;
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Internal score given to <code>AggrAssemblyTestConfig</code> records.
     * </p>
     * <p>
     * The <code>Score</code> instances maintain the "score" given to a particular processor configuration.
     * The "score" is a measure of the processor performance for a given configuration and is updated
     * using the method <code>{@link AggrAssemblyConfigScore#score(QueryAssemblyTestResult)}</code>. 
     * The configuration for the <code>Score</code> is maintained in the <code>{@link #recCfg}</code> attribute
     * set at creation. 
     * </p>
     * <p>
     * Within a <code>AggrAssemblyConfigScore</code> object, <code>Score</code> instances are maintained
     * within the <code>{@link AggrAssemblyConfigScore#mapIndexScore}</code> attribute.  There they are indexed
     * by the corresponding <code>AggrAssemblyTestConfig</code> record containing the assembly configuration. 
     * </p>
     * 
     * @see ConfigResultScore#score(ConfigResult)
     */
    public static class Score {

        
        //
        // Creator
        //
        
        /** 
         * Creates a new, empty <code>Score</code> instance for the given <code>AggAssemblyTestConfig</code> configuration.
         *   
         * @param recCfg  index record containing the score's processor configuration
         * 
         * @return  a new, empty <code>Score</code> instance for the given processor configuration
         */
        private static Score from(AggrAssemblyTestConfig recIndex) {
            return new Score(recIndex);
        }


        //
        // Class Tools
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
        // Score Defining Attributes
        //
        
        /** AggAssemblyTestConfig record for the score (back pointer) - the score is for this record configuration */
        private  final AggrAssemblyTestConfig       recCfg;

        
        //
        // Score Resources
        //

        /** Map of (request, hits) request hit counters seen while scoring */
        private final Map<DpDataRequest, Integer>   mapRqstToHits = new HashMap<>();


        //
        // Score State Variables
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
        
        
        /** The average number of sampled blocks per aggregate */
        private int     cntBlksTotAvg = 0;
        
        /** The average number of clocked sampled blocks per aggregate */
        private int     cntBlksClkdAvg = 0;
        
        /** The average number of timestamp list sampled blocks per aggregate */
        private int     cntBlksTmsLstAvg = 0;
        
        /** The average number of super-domain sampled blocks per aggregate */
        private int     cntBlksSupDomAvg = 0;
        
        
        /** The number of test failures for the score test configuration */
        private int     cntTestFailures = 0;


        //
        // Constructor
        //

        /**
         * <p>
         * Constructs a new, empty <code>Score</code> instance for the given <code>AggAssemblyTestConfig</code>.
         * </p>
         *
         * @param recCfg  processor configuration index
         */
        private Score(AggrAssemblyTestConfig recCfg) {
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


        //
        // Operations
        //
        
        /**
         * <p>
         * Adds in the given test result to the current running score.
         * </p>
         * <p>
         * The given <code>SampledAggregate</code> assembly test result field values are used to 
         * update the running score applying to the <code>Score</code> instance's configuration.
         * The value of the current data rate average value is returned as a convenience.
         * </p>
         * <p>
         * If the <code>{@link QueryAssemblyTestResult#recTestStatus()}</code> field indicates
         * a test failure the attribute <code>{@link #cntTestFailures}</code> is incremented
         * and the test result record is ignored (i.e., the method returns immediately).
         * </p>
         * 
         * @apiNote
         * It is assumed that the given <code>QueryAssemblyTestResult</code> applies to this
         * <code>Score</code> instance. Specifically, the configuration extracted by
         * <code>{@link AggrAssemblyTestConfig#from(QueryAssemblyTestResult)}</code> must
         * be equivalent to the <code>{@link #recCfg}</code> record attribute of this instance.
         * Otherwise an exception is thrown.
         *  
         * @param recResult the test results containing the sampled aggregate assembly results
         * 
         * @return  the current running average data rate
         * 
         * @throws  IllegalArgumentException    the argument does not apply to this <code>Score</code> instance
         */
        public double   addInResult(QueryAssemblyTestResult recResult) throws IllegalArgumentException {
            
            // Check argument
            AggrAssemblyTestConfig  recResultCfg = AggrAssemblyTestConfig.from(recResult);
            if (!this.recCfg.equals(recResultCfg)) {
                throw new IllegalArgumentException(
                        JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Argument does NOT apply to this Score instance.");
            }
            
            // Check if this is a test failure
            if (recResult.recTestStatus().isFailure()) {
                this.cntTestFailures++;
                
                return this.dblRateAvg;
            }

            // Initialize min/max data rates if this is the first call
            if (this.cntHitsTot == 0) {
                this.dblRateMax = recResult.dblRateAggAssm();
                this.dblRateMin = recResult.dblRateAggAssm();
            } 
            
            // Add in the data rate scores to the running averages 
            this.dblRateAvg *= this.cntHitsTot;       // total the previous rate
            this.dblRateAvg += recResult.dblRateAggAssm();    // add the new rate

            this.dblRateSqrd *= this.cntHitsTot;
            this.dblRateSqrd += recResult.dblRateAggAssm()*recResult.dblRateAggAssm();

            // Add in the sampled aggregate properties to the running averages
            this.cntBlksTotAvg *= this.cntHitsTot;
            this.cntBlksTotAvg += recResult.cntBlksAggTot();
            
            this.cntBlksClkdAvg *= this.cntHitsTot;
            this.cntBlksClkdAvg += recResult.cntBlksAggClkd();
            
            this.cntBlksTmsLstAvg *= this.cntHitsTot;
            this.cntBlksTmsLstAvg += recResult.cntBlksAggTmsLst();
            
            this.cntBlksSupDomAvg *= this.cntHitsTot;
            this.cntBlksSupDomAvg += recResult.cntBlksAggSupDom();

            // Increment the hit counter then compute averages
            this.cntHitsTot++;                         // increment hit counter
            
            this.dblRateAvg /= this.cntHitsTot;       // compute new avg rate
            this.dblRateSqrd /= this.cntHitsTot;      // compute new 2nd moment
            
            this.cntBlksTotAvg /= this.cntHitsTot;
            this.cntBlksClkdAvg /= this.cntHitsTot;
            this.cntBlksTmsLstAvg /= this.cntHitsTot;
            this.cntBlksSupDomAvg /= this.cntHitsTot;

            // Update min/max data rate values
            if (recResult.dblRateAggAssm() > this.dblRateMax)
                this.dblRateMax = recResult.dblRateAggAssm();
            if (recResult.dblRateAggAssm() < this.dblRateMin)
                this.dblRateMin = recResult.dblRateAggAssm();
            
            // Get the data request and update the hit count for that request
            DpDataRequest   rqst = recResult.recTestCase().rqstFinal();
            
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
         * @param strPad    left-side padding for parameter descriptions (or <code>null</code>) 
         */
        public void printOut(PrintStream ps, String strPad) {
            if (strPad == null)
                strPad = "";
            String  strPadd = strPad + "  ";
            
            ps.println(strPad + "Aggregate Assembly Configuration");
            this.recCfg.printOut(ps, strPadd);
            ps.println(strPad + "Score Parameters Aggregate Assembly Configuration");
            ps.println(strPad + "  Total number of cfg.  hits     : " + this.cntHitsTot);
            ps.println(strPad + "  Number test failures for cfg.  : " + this.cntTestFailures);
            ps.println(strPad + "  Minimum data rate (MBps)       : " + this.dblRateMin);
            ps.println(strPad + "  Maximum data rate (MBps)       : " + this.dblRateMax);
            ps.println(strPad + "  Average data rate (MBps)       : " + this.dblRateAvg);
            ps.println(strPad + "  Data rate 2nd moment (MBps)^2  : " + this.dblRateSqrd);
            ps.println(strPad + "  Data rate stand. dev. (MBps)   : " + this.dataRateStd());
            ps.println(strPad + "  Average total block count      : " + this.cntBlksTotAvg);
            ps.println(strPad + "  Avg. no. clocked blocks        : " + this.cntBlksClkdAvg);
            ps.println(strPad + "  Avg. no. timestamp list blocks : " + this.cntBlksTmsLstAvg);
            ps.println(strPad + "  Avg. no. super-domain blocks   : " + this.cntBlksSupDomAvg);
            for (Map.Entry<DpDataRequest, Integer> entry : this.mapRqstToHits.entrySet()) {
                String  strRqstId = entry.getKey().getRequestId();
                Integer cntHits = entry.getValue();

                ps.println(strPad + "  Request " + strRqstId + " hit count :" + cntHits);
            }
        }
    }
    
    
    //
    // AggrAssemblyConfigScore Class Resources
    //
    
    /** List of all possible configuration indexes for the given - managed by {@link AggrAssemblyConfigScore#retrieveIndexFor(QueryAssemblyTestResult)} */
    private static final List<AggrAssemblyTestConfig>    LST_CONFIGS = new LinkedList<>();


    //
    // AggrAssemblyConfigScore Instance Resources
    //

    /** Map of indexes to result scores */
    private final Map<AggrAssemblyTestConfig, Score>    mapIndexScore = new HashMap<>();

//    /** Map of (name, request) pairs used in data request monitoring */
//    private final Collection<DpDataRequest>             setRqstHits;

    
    //
    // AggrAssemblyConfigScore Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>AggrAssemblyConfigScore</code> instance.
     * </p>
     *
     */
    public AggrAssemblyConfigScore() {
    }
    
    
    //
    // AggrAssemblyConfigScore Operations
    //
    
    /**
     * <p>
     * Scores individual result record and saves the scoring results into the collection of
     * scoring based upon configuration (i.e., the equivalent <code>AggrAssemblyTestConfig</code> configuration).
     * </p>
     * <p>
     * Here the running score for a configuration (by <code>AggrAssemblyTestConfig</code>) are the date rate statistics 
     * (e.g., such as the average <code>{@link Score#dblRate}</code> field).
     * Note that scoring also includes the various hit counts for the configuration (i.e., <code>AggrAssemblyTestConfig</code>)
     * and other <code>ConfigResult</code> parameters.
     * </p>
     * <p>
     * As a convenience the method returns the new data rate average value.  
     * </p>
     * 
     * @param recResult the test result to be scored (containing the test configuration)
     * 
     * @return  new data rate average value of the configuration score (for convenience)
     */
    public double   score(QueryAssemblyTestResult recResult) {

        // Get the index for the configuration w/in the result
        AggrAssemblyTestConfig recIndex = AggrAssemblyConfigScore.retrieveIndexFor(recResult);

        // Get the Score instance for the configuration index - make new Score if none exists
        Score   score = this.mapIndexScore.get(recIndex);

        if (score == null) { 
            score = Score.from(recIndex);

            this.mapIndexScore.put(recIndex, score);
        }

        // Add in the test results to the score
        double  dblRateAvg = score.addInResult(recResult);
        
        return dblRateAvg;
    }

    /**
     * <p>
     * Scores the entire collection of result records and saves the scoring results into the collection
     * of scores based upon configuration (i.e., the equivalent <code>AggrAssemblyTestConfig</code> configuration).
     * </p>
     * <p>
     * This method is essentially a convenience, deferring to <code>{@link #score(QueryAssemblyTestResult)}</code> for
     * repeated invocation on each element of the argument collection.  The returned value is simply the 
     * average of all data rates scored and returned by <code>{@link #score(QueryAssemblyTestResult)}</code>.
     * </p>
     * 
     * @param setResults    collection of <code>QueryAssemblyTestResult</code> records to be scored
     * 
     * @return  new data rate average value of the collective configuration score
     */
    public double score(Collection<QueryAssemblyTestResult>  setResults) {

        double  dblRateAvg = setResults
                .stream()
                .mapToDouble(rec -> this.score(rec))
                .sum()
                /setResults.size();

        return dblRateAvg;
    }

    /**
     * <p>
     * Scores result records within argument collection that have data rates greater than or equal to 
     * the given minimum value.  
     * </p>
     * <p>
     * Only those result records with attribute <code>{@link QueryAssemblyTestResult#dblRateAggAssm()}</code> &ge; 
     * the given minimum rate will be scored.  Specifically, for each result record <code>r</code>
     * within the collection we have
     * <pre>
     *   <code>r.dblRateAggrAssm()</code> &ge; <code>dblRateMin</code> &rArr; <code>{@link #score(r)}</code>
     * </pre>  
     * Thus, this method is essentially a convenience, deferring to <code>{@link #score(QueryAssemblTestResult)}</code> 
     * for repeated invocation on each element of the argument satisfying the above condition.  
     * The returned value is the number of <code>QueryAssemblyTestResult</code> records that satisfied the condition
     * and were scored. 
     * </p>
     * 
     * @param setResults    collection of <code>QueryAssemblyTestResult</code> records to be scored
     * @param dblRateMin    minimum data rate for scoring
     * 
     * @return  the number of <code>QueryAssemblyTestResult</code> records that were scored (i.e., satisfied the minimum data rate)
     */
    public int  score(Collection<QueryAssemblyTestResult> setResults, double dblRateMin) {

        // Initialize counter and score results with valid data rates
        int     cntScores = 0;

        for (QueryAssemblyTestResult recResult : setResults) {

            // Check rate
            if (recResult.dblRateAggAssm() >= dblRateMin) {
                this.score(recResult);
                cntScores++;
            }
        }

        return cntScores;
    }

    
    //
    // AggrAssemblyConfigScore State Inquiry
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
    public int  getConfigurationCount(QueryAssemblyTestResult recResult) {

        // Get the index for the configuration w/in the result
        AggrAssemblyTestConfig recIndex = AggrAssemblyConfigScore.retrieveIndexFor(recResult);

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
    public AggrAssemblyConfigScore.Score  getScoreWithBestAvgRate() {

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
    public AggrAssemblyConfigScore.Score   getScore(QueryAssemblyTestResult recResult) {
        // Get the index for the configuration w/in the result
        AggrAssemblyTestConfig  recIndex = AggrAssemblyConfigScore.retrieveIndexFor(recResult);

        // Get the current score record for the index - make new score if none exists
        Score   score = this.mapIndexScore.get(recIndex);

        return score;
    }

    /**
     * <p>
     * Returns the set of all <code>Score</code> records ordered by configuration hit count, large to small.
     * </p>
     * 
     * @return  all the <code>QueryAssemblyTestResult</code> score records so far
     */
    public SortedSet<Score> recoverScoresByHits() {

        SortedSet<Score>    setScores = new TreeSet<>(Score.createHitOrdering());

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
     * @return  all the <code>QueryAssemblyTestResult</code> score records so far
     */
    public SortedSet<Score> recoverScoresByRates() {
        SortedSet<Score>    setScores = new TreeSet<>(Score.createRateOrdering());

        for (Score score : this.mapIndexScore.values()) {
            setScores.add(score);
        }

        return setScores;
    }

    /**
     * <p>
     * Prints out the given configuration results scoring by descending hit counts.
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
    public void printOutByHits(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";

        SortedSet<Score>    setScores = this.recoverScoresByHits();

//        ps.println("Sampled Aggregate Assembly Scoring by Hit Count");
        printOut(ps, setScores, strPad);
    }

    /**
     * <p>
     * Prints out the given configuration results scoring ordered by descending average data rates.
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
    public void printOutByRates(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        SortedSet<Score>    setScores = this.recoverScoresByRates();

//        ps.println("Sampled Aggregate Assembly Scoring by Data Rates");
        printOut(ps, setScores, strPad);
    }


    // 
    // Support Methods
    //

    /**
     * <p>
     * Returns the internal index corresponding to the given <code>ConfigResult</code record.
     * </p>
     * <p>
     * The static list of index records, <code>{@link #LST_CONFIGS}</code> is searched for an
     * index equivalent to the given test configuration.  If the list currently contains the
     * equivalent index it is returned.  Otherwise, a new index is created, added to the list,
     * and returned.
     * </p> 
     * 
     * @param recResult    processor configuration performance result record to cross reference
     * 
     * @return  equivalent <code>AggrAssemblyTestConfig</code> configuration record for the given argument
     */
    private static AggrAssemblyTestConfig retrieveIndexFor(QueryAssemblyTestResult recResult) {

        // Check if there already is an equivalent configuration index in list
        AggrAssemblyTestConfig      recRsltConfig = AggrAssemblyTestConfig.from(recResult);
        
        for (AggrAssemblyTestConfig recConfig : LST_CONFIGS) {
            if (recConfig.equals(recRsltConfig))
                return recConfig;
        }

        // No indexes for the given test configuration
        LST_CONFIGS.add(recRsltConfig);

        return recRsltConfig;
    }

    /**
     * <p>
     * Prints out a textual description of the ordered scores to the given output.
     * </p>
     * 
     * @param ps        output for the text description
     * @param setScores ordered set of configuration scores to print out
     * @param strPad    left-hand white space padding
     */
    private static void printOut(PrintStream ps, SortedSet<Score> setScores, String strPad) {
        
        if (strPad == null)
            strPad = "";
        String strPadd = strPad + "  ";

        // Compute collective data
        final int       cntScores = setScores.size();
        final int       cntHitsTot = setScores.stream().mapToInt(score -> score.cntHitsTot).sum();
        final double    dblRateAvg = setScores.stream().mapToDouble(score -> score.dblRateAvg).sum()/cntScores;
        final double    dblRateStd = setScores.stream().mapToDouble(score -> score.dataRateStd()).sum()/cntScores;

        ps.println(strPad + "Total number of configuration scores = " + cntScores);
        ps.println(strPad + "Total number of test result hits     = " + cntHitsTot);
        ps.println(strPad + "Total data rate average (MBps)       = " + dblRateAvg);
        ps.println(strPad + "Total data rate standard dev. (MBps) = " + dblRateStd);
        
        ps.println(strPad + "Aggregate Assembly Configuration Scores");
        for (Score score : setScores) {

            score.printOut(ps, strPadd);
            ps.println();
        }
    }
    

}
