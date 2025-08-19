/*
 * Project: dp-api-common
 * File:	ConfigScorerBase.java
 * Package: com.ospreydcs.dp.jal.tools.common
 * Type: 	ConfigScorerBase
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
 * @since Aug 18, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * Base class for scoring test configurations in collections of test results.
 * </p>
 * <p>
 * Child classes are intended for the "scoring" of particular test conditions, or "configuration" for 
 * repeated test evaluations.  Configurations are implemented as Java <code>{@link Record}</code> records
 * which contain a subset of the test case parameters (or all).  The configuration is represented by
 * generic type <code>Config</code> while the result of a test case evaluation is represented by generic
 * type <code>Result</code>.
 * </p>
 * <p>
 * <h2>Usage</h2>  
 * The child class generally implements an enclosed class derived from
 * <code>{@link ConfigScoreBase}</code> to do the scoring for a particular configuration.  
 * Note that derived class is the template (generic) parameter <code>Score</code> for this class.  The
 * <code>Score</code> implementation performs the actually scoring of test results within a collection of 
 * <code>Result</code> records.
 * This class, <code>ConfigScorerBase</code>, manages the collection of <code>Config</code> records seen
 * in any collection of <code>Result</code> records.
 * <p>
 * Within a <code>ConfigScorerBase</code> object, <code>ConfigScoreBase</code> instances are maintained
 * within the <code>{@link ConfigScorerBase#mapIndexScore}</code> attribute.  There they are indexed
 * by the corresponding <code>Config</code> record containing the assembly configuration. 
 * See <code>{@link ConfigScoreBase}</code> class documentation for more information.
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
 * @author Christopher K. Allen
 * @since Aug 18, 2025
 *
 * @param <Config>  Configuration record for test case evaluations (with <code>{@link Record#equals(Object)}</code> override)
 * @param <Result>  The test result for a given test case (must contain the configuration information)
 * @param <Score>   The score obtained from a collection of <code>Result</code> records for a given test configuration <code>Config</code> 
 */
public abstract class ConfigScorerBase<Config extends Record, Result extends Record, Score extends ConfigScoreBase<Config, Result>> {


    //
    // Abstract Methods
    //

    /**
     * <p>
     * Extracts the test configuration for the test result from the given result record.
     * </p>
     * <p>
     * This method is required for support method <code>{@link #retrieveIndexFor(Record)}</code>.
     * It is used to extract the configuration of a test result and compared it against all
     * managed configurations using equivalence with the <code>{@link Record#equals(Object)}</code>
     * override.
     * </p>
     * 
     * @param recResult a <code>Result</code> test record containing the configurations for the test
     * 
     * @return  the test case configuration for the given test result
     */
    protected abstract Config   extractConfiguration(Result recResult);
    
//    /**
//     * <p>
//     * Extracts the target data rate from the given result record.
//     * </p>
//     * <p>
//     * The returned value is used to compute the scoring for the given result.  Note that the 
//     * configuration information must also be available.
//     * </p>
//     * 
//     * @param recResult a <code>Result</code> test result record containing data rate (and other results) 
//     * 
//     * @return  the data rate for the test result (in MBps)
//     */
//    protected abstract double    extractDataRate(Result recResult);
    
    /**
     * <p>
     * Creates a new, empty <code>Score</code> instance from the given configuration record.
     * </p>
     * <p>
     * This method is required for method <code>{@link #score(Record)}</code>.  It is used to create
     * new <code>Score</code> instances whenever a new configuration is encountered.
     * </p>
     * 
     * @param recConfig the configuration record for which the returned <code>Score</code> instance is based
     * 
     * @return  a new <code>Score</code> instance based upon the given configuration record
     */
    protected abstract Score    newScore(Config recConfig);


    //
    // Instance Resources
    //

    /** List of all possible configuration indexes for the given - managed by {@link #retrieveIndexFor(Result)} */
    private final List<Config>          lstConfigIndexes = new LinkedList<>();

    /** Map of indexes to result scores */
    private final Map<Config, Score>    mapIndexScore = new HashMap<>();

    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>ConfigScorerBase</code> instance.
     * </p>
     *
     */
    protected ConfigScorerBase() {
    }
    
    /**
     * <p>
     * Constructs a new, initialized <code>ConfigScorerBase</code> instance.
     * </p>
     * <p>
     * This method is essentially a convenience, deferring to <code>{@link #score(Collection)}</code> for
     * scoring the entire argument collection upon construction.  
     * </p>
     * 
     * @param setResults    collection of <code>Result</code> records to be scored
     * 
     * @see #score(Collection)
     */
    protected ConfigScorerBase(Collection<Result> setResults) {
        
        this.score(setResults);
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Scores individual result record and saves the scoring results into the collection of
     * scoring based upon configuration (i.e., the equivalent <code>Config</code> configuration).
     * </p>
     * <p>
     * Here the running score for a configuration (by <code>Config</code>) are the date rate statistics 
     * (e.g., such as the average <code>{@link Score#getDataRateAvg}</code> field).
     * Note that scoring also includes the various hit counts for the configuration (i.e., <code>Config</code>)
     * and other <code>Result</code> parameters.
     * </p>
     * <p>
     * As a convenience the method returns the new data rate average value.  
     * </p>
     * 
     * @param recResult the test result to be scored (containing the test configuration)
     * 
     * @return  new data rate average value of the configuration score (for convenience)
     */
    public double   score(Result recResult) {

        // Get the index for the configuration w/in the result
        Config recIndex = this.retrieveIndexFor(recResult);

        // Get the Score instance for the configuration index - make new Score if none exists
        Score   score = this.mapIndexScore.get(recIndex);

        if (score == null) { 
            score = this.newScore(recIndex);

            this.mapIndexScore.put(recIndex, score);
        }

        // Add in the test results to the score
        double  dblRateAvg = score.addInResult(recResult);
        
        return dblRateAvg;
    }

    /**
     * <p>
     * Scores the entire collection of result records and saves the scoring results into the collection
     * of scores based upon configuration (i.e., the equivalent <code>Config</code> configuration).
     * </p>
     * <p>
     * This method is essentially a convenience, deferring to <code>{@link #score(Result)}</code> for
     * repeated invocation on each element of the argument collection.  The returned value is simply the 
     * average of all data rates scored and returned by <code>{@link #score(Result)}</code>.
     * </p>
     * 
     * @param setResults    collection of <code>Result</code> records to be scored
     * 
     * @return  new data rate average value of the collective configuration score
     */
    public double score(Collection<Result>  setResults) {

        double  dblRateAvg = setResults
                .stream()
                .mapToDouble(rec -> this.score(rec))
                .sum()
                /setResults.size();

        return dblRateAvg;
    }

//    /**
//     * <p>
//     * Scores result records within argument collection that have data rates greater than or equal to 
//     * the given minimum value.  
//     * </p>
//     * <p>
//     * Only those result records with attribute <code>{@link #extractDataRate()}</code> &ge; 
//     * the given minimum rate will be scored.  Specifically, for each result record <code>r</code>
//     * within the collection we have
//     * <pre>
//     *   <code>extractDataRate(r)</code> &ge; <code>dblRateMin</code> &rArr; <code>{@link #score(r)}</code>
//     * </pre>  
//     * Thus, this method is essentially a convenience, deferring to <code>{@link #score(Result)}</code> 
//     * for repeated invocation on each element of the argument satisfying the above condition.  
//     * The returned value is the number of <code>Result</code> records that satisfied the condition
//     * and were scored. 
//     * </p>
//     * 
//     * @param setResults    collection of <code>Result</code> records to be scored
//     * @param dblRateMin    minimum data rate for scoring
//     * 
//     * @return  the number of <code>Result</code> records that were scored (i.e., satisfied the minimum data rate)
//     */
//    public int  score(Collection<Result> setResults, double dblRateMin) {
//
//        // Initialize counter and score results with valid data rates
//        int     cntScores = 0;
//
//        for (Result recResult : setResults) {
//
//            // Check rate
//            if (this.extractDataRate(recResult) >= dblRateMin) {
//                this.score(recResult);
//                cntScores++;
//            }
//        }
//
//        return cntScores;
//    }

    
    //
    // State Inquiry
    //

    /**
     * <p>
     * Gets the hit count <code>Score</code> field for the given configuration results.
     * </p>
     * <p>
     * The method extracts the test configuration from the given test result then defers to method
     * </code>{@link #getConfigurationCount(Record)}</code>. 
     * </p>
     * 
     * @param recResult    result record containing test configuration
     * 
     * @return  number of scoring instances within the given test result configuration or 0 if not scored
     * 
     * @see #getConfigurationCount(Record)
     */
    public int  getConfigurationCountFromResult(Result recResult) {

        // Get the index for the configuration w/in the result
        Config recIndex = this.retrieveIndexFor(recResult);
        
        if (recIndex == null)
            return 0;

        return this.getConfigurationCount(recIndex);
    }
    
    /**
     * <p>
     * Gets the hit count <code>Score</code> field for the given configuration.
     * </p>
     * <p>
     * The method searches from the given configuration in its list of managed configurations.  
     * If found, it returns the number of test result occurrences within that configuration 
     * (i.e., <code>{@link ConfigScoreBase#getHitCount()}</code>).  Otherwise a 0 value is returned.
     * </p>
     * 
     * @param recConfig the configuration record under scrutiny
     * 
     * @return  number of scoring instances with this configuration or 0 not scored
     */
    public int  getConfigurationCount(Config recConfig) {
        
        // Get the current score record for the index - return 0 if none exists
        Score   recScore = this.mapIndexScore.get(recConfig);

        if (recScore == null)
            return 0;

        return recScore.getHitCount();
    }
    
    /**
     * <p>
     * Returns the number of test result failures for the given test result record.
     * </p>
     * <p>
     * The method extracts the test configuration from the given test result then defers to method
     * <code>{@link #getConfigurationFailures(Record)}</code>.
     * 
     * @param recResult    result record containing test configuration
     * 
     * @return  the number of test failures for the given result configuration, 
     *          or <code>null</code> if configuration not scored
     */
    public int  getResultFailures(Result recResult) {
        
        // Get the index for the configuration w/in the result
        Config recIndex = this.retrieveIndexFor(recResult);
        
        if (recIndex == null)
            return 0;

        // Get the number of failures for the configuration
        return this.getConfigurationFailures(recIndex);
    }
    
    /**
     * <p>
     * Returns the number of test result failures within the given configuration.
     * </p>
     * <p>
     * The method searches from the given configuration in its list of managed configurations.  
     * If found, it returns the number of test result failures for that configuration 
     * (i.e., <code>{@link ConfigScoreBase#getTestFailures()}</code>).  Otherwise a <code>null</code> 
     * value is returned.
     * </p>
     * 
     * @param recConfig the configuration under scrutiny
     * 
     * @return  the number of test failures for the given configuration, or <code>null</code> if configuration not scored
     */
    public Integer  getConfigurationFailures(Config recConfig) {
        
        // Get the current score record for the index - return zero if none exists
        Score   recScore = this.mapIndexScore.get(recConfig);

        if (recScore == null)
            return null;

        return recScore.getHitCount();
    }

    /**
     * <p>
     * Finds and returns the score with the largest average data rate.
     * </p>
     * <p>
     * Parses the container of all current scores and returns the <code>Score</code> with the
     * largest average data rate, that is, the largest value of <code>{@link ConfigScoreBase#getDataRateAvg()}</code>.
     * </p>
     * 
     * @return  <code>Score</code> record with largest data rate, or <code>null</code> if none exists
     */
    public Score  getScoreWithBestAvgRate() {

        // Find the score with largest average rate
        Optional<Score> optScore = this.mapIndexScore
                .values()
                .stream()
                .reduce((s1,s2) -> {
                    if (s1.getDataRateAvg() > s2.getDataRateAvg())
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
     * Gets the score for the given test result configuration.
     * </p>
     *  
     * @param recResult    result record containing test configuration
     * 
     * @return  current configuration score, or <code>null</code> if none
     */
    public Score   getScore(Result recResult) {
        // Get the index for the configuration w/in the result
        Config  recIndex = this.retrieveIndexFor(recResult);

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

        printOut(ps, setScores, strPad, "(Most Hits First)");
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

        printOut(ps, setScores, strPad, "(Best Rate First)");
    }


    
    // 
    // Support Methods
    //

    /**
     * <p>
     * Returns the internal index corresponding to the given <code>Result</code> record.
     * </p>
     * <p>
     * The list of index records, <code>{@link #lstConfigIndexes}</code> is searched for an
     * index equivalent to the given test configuration.  If the list currently contains the
     * equivalent index it is returned.  Otherwise, a new index is created, added to the list,
     * and returned.
     * </p> 
     * 
     * @param recResult    test result record to cross reference
     * 
     * @return  equivalent <code>Config</code> configuration record for the given argument
     */
    private Config retrieveIndexFor(Result recResult) {

        // Check if there already is an equivalent configuration index in list
        Config      recRsltConfig = this.extractConfiguration(recResult);
        
        for (Config recConfig : lstConfigIndexes) {
            if (recConfig.equals(recRsltConfig))
                return recConfig;
        }

        // No indexes for the given test configuration
        lstConfigIndexes.add(recRsltConfig);

        return recRsltConfig;
    }

    /**
     * <p>
     * Prints out a textual description of the ordered scores to the given output.
     * </p>
     * <p>
     * First gives a summary of the argument <code>Score</code> collection then iterates
     * through each <code>{@link Score#printOut(PrintStream, String)}</code> method.
     * </p>
     * 
     * @param ps            output for the text description
     * @param setScores     ordered set of configuration scores to print out
     * @param strPad        (optional) left-hand white space padding
     * @param strOrdering   (optional) text description of the ordering (or <code>null</code>)
     */
    private void printOut(PrintStream ps, SortedSet<Score> setScores, String strPad, String strOrdering) {
        
        if (strPad == null)
            strPad = "";
        String strPadd = strPad + "  ";
        
        if (strOrdering == null)
            strOrdering = "";

        // Compute collective data
        final int       cntScores = setScores.size();
        final int       cntHitsTot = setScores.stream().mapToInt(score -> score.getHitCount()).sum();
        final int       cntFailures = setScores.stream().mapToInt(score -> score.getTestFailures()).sum();
        final double    dblRateAvg = setScores.stream().mapToDouble(score -> score.getDataRateAvg()).sum()/cntScores;
        final double    dblRateStd = setScores.stream().mapToDouble(score -> score.dataRateStd()).sum()/cntScores;

        ps.println(strPad + "Total number of configuration scores = " + cntScores);
        ps.println(strPad + "Total number of test results scored  = " + cntHitsTot);
        ps.println(strPad + "Total number of test result failures = " + cntFailures);
        ps.println(strPad + "Total data rate average (MBps)       = " + dblRateAvg);
        ps.println(strPad + "Total data rate standard dev. (MBps) = " + dblRateStd);
        
        ps.println(strPad + "Configuration Scores " + strOrdering);
        for (Score score : setScores) {

            score.printOut(ps, strPadd);
            ps.println();
        }
    }
    
}
