/*
 * Project: dp-api-common
 * File:	QueryRequestProcessorTest.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	QueryRequestProcessorTest
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jan 23, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.impl;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases and performance evaluations for class <code>QueryRequestProcessor</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 23, 2025
 *
 */
public class QueryRequestProcessorTest {

    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Record containing all the configuration parameters for a <code>QueryRequestProcessor</code> test request.
     * </p>
     *
     * @param bolCorrConcurrent correlate using concurrency (multi-threading) enabled/disabled flag
     * @param bolCorrStreaming  correlate while streaming enabled/disabled flag
     * @param CNT_MAX_STREAMS     maximum number of allowable gRPC data streams allowed for request recovery
     * @param cntMaxSources     maximum number of allowable data sources per composite request
     * @param durMaxRange       maximum time range duration within composite request
     *
     * @author Christopher K. Allen
     * @since Jan 24, 2025
     *
     */
    public static record    TestConfig(boolean bolCorrConcurrent, boolean bolCorrStreaming, int cntMaxStreams, int cntMaxSources, Duration durMaxRange) {
        
        
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
         * @param CNT_MAX_STREAMS     maximum number of allowable gRPC data streams allowed for request recovery
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
         * @param prcrQuery the <code>QueryRequestProcessor</code> to configure for data recovery
         */
        public void configure(QueryRequestProcessor prcrQuery) {
            
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
    
    /**
     * <p>
     * Record containing the results of a <code>{@link QueryRequestProcessor#processRequest(DpDataRequest)}</code> operation
     * for the given processor configuration and time-series data request.
     * </p>
     *
     * @param dblRate       the processing rate for the given configuration and time-series data request  
     * @param indCase       optional case index
     * @param cntMessages   number of response messages recovered and processed
     * @param szAlloc       total memory allocation recovered and processed
     * @param cntBlocks     number of correlated data blocks produced
     * @param durRequest    the duration required for the request recover and processing
     * @param recConfig     the test configuration for the <code>QueryRequestProcessor</code> 
     * @param request       the time-series data request used in the test
     * @param strRqstName   the (optional) time-series data request name
     * @param lstCmpRqsts   the composite requests generated by the request processor 
     *  
     * @author Christopher K. Allen
     * @since Jan 25, 2025
     *
     */
    public static record    ConfigResult(double dblRate, int indCase, int cntMessages, long szAlloc, int cntBlocks, Duration durRequest, TestConfig recConfig, DpDataRequest request, String strRqstName, List<DpDataRequest> lstCmpRqsts) {
        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates a new,  initialized <code>ConfigResult</code> record according to the given arguments.
         * </p>
         * 
         * @param dblRate       the processing rate for the given configuration and time-series data request  
         * @param indCase       optional case index
         * @param cntMessages   number of response messages recovered and processed
         * @param szAlloc       total memory allocation recovered and processed
         * @param cntBlocks     number of correlated data blocks produced
         * @param durRequest    the duration required for the request recover and processing
         * @param recConfig     the test configuration for the <code>QueryRequestProcessor</code> 
         * @param request       the time-series data request used in the test
         * @param strRqstName   the (optional) time-series data request name 
         * @param lstCmpRqsts   the composite requests generated by the request processor 
         * 
         * @return  a new <code>ConfigResult</code> record with the given parameters
         */
        public static ConfigResult  from(double dblRate, int indCase, int cntMessages, long szAlloc, int cntBlocks, Duration durRequest, TestConfig recConfig, DpDataRequest request, String strRqstName, List<DpDataRequest> lstCmpRqsts) {
            return new ConfigResult(dblRate, indCase, cntMessages, szAlloc, cntBlocks, durRequest, recConfig, request, strRqstName, lstCmpRqsts);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Prints out a description of the current record on the given print stream.
         * </p>
         * 
         * @param os        print stream for record description
         * @param strPad    optional line padding for parameters
         */
        public void printOut(PrintStream os, String strPad) {
            
            // Print out results
            os.println(strPad + "Processor Configuration Test - Case #" + this.indCase);
            os.println(strPad + "  Processing rate (Mbps)  : " + this.dblRate);
//            os.println(strPad + "  Test case index         : " + this.indCase);
            os.println(strPad + "  Messages recovered      : " + this.cntMessages);
            os.println(strPad + "  Processed data blocks   : " + this.cntBlocks);
            os.println(strPad + "  Processed bytes         : " + this.szAlloc);
            os.println(strPad + "  Request duration        : " + this.durRequest);
            os.println(strPad + "  DpDataRequest name      : " + this.strRqstName);

            os.println(strPad + "QueryRequestProcessor Configuration");
            this.recConfig.printOut(os, strPad + strPad);

            os.println(strPad + "Request Decomposition");
            os.println(strPad + "  Composite request count : " + this.lstCmpRqsts.size());
            int indRqst = 1;
            String  strHdr = strPad + strPad;
            for (DpDataRequest rqstCmp : this.lstCmpRqsts) {
                os.println(strHdr + "Composite Request #" + indRqst);
                os.println(strHdr + "  gRPC stream type   : " + rqstCmp.getStreamType());
                os.println(strHdr + "  data source count  : " + rqstCmp.getSourceCount());
                os.println(strHdr + "  duration (seconds) : " + rqstCmp.rangeDuration().toSeconds());
                os.println(strHdr + "  time interval      : " + rqstCmp.range());
                os.println(strHdr + "  domain size        : " + rqstCmp.approxDomainSize());
                indRqst++;
            }
        }
        
        /**
         * <p>
         * Comparator class for sorted collections of <code>ConfigResult</code> records.
         * </p>
         * <p>
         * Provides a reverse order of <code>ConfigResult</code> records by <code>{@link ConfigResult#dblRate}</code>
         * field.
         * </p>
         */
        public static class ResultOrder implements Comparator<ConfigResult> {

            /**
             * @return  a <code>Comparator</code> interface for reverse order of <code>ConfigResult</code> records by rate field
             */
            public static ResultOrder   create() {
                return new ResultOrder();
            }
            
            //
            // Comparator<ConfigResult> Interface
            //
            
            @Override
            public int compare(ConfigResult r1, ConfigResult r2) {
                if (r1.dblRate > r2.dblRate)
                    return -1;
                else if (r1.dblRate == r2.dblRate)
                    return 0;
                else
                    return 1;
            }
            
        }
    }
    
    
    /**
     * <p>
     * Class for computing scoring and statistics of a collection of <code>ConfigResult</code> records.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jan 27, 2025
     *
     */
    public static class ConfigResultScore {
        
        /**
         * Record used for indexing <code>ConfigResult</code> records by fields.
         */
        public static record Index(int cntMaxStreams, boolean bolCorrCon, boolean bolCorrStrm) {
            
            /** Creator */
            public static Index from(int cntMaxStreams, boolean bolCorrCon, boolean bolCorrStrm) {
                return new Index(cntMaxStreams, bolCorrCon, bolCorrStrm);
            }
            
            /** The "null" index */
            public static Index     NULL_INDEX = Index.from(0, false, false);
            
            /**
             * <p>
             * Checks if given <code>TestConfig</code> is equivalent to this one.
             * 
             * @param recIndex  <code>TestConfig</code> record under comparison
             * 
             * @return  <code>true</code> if index record fields are equal
             */
            public boolean  isIndex(TestConfig recCfg) {
                return this.cntMaxStreams==recCfg.cntMaxStreams &&
                        this.bolCorrCon==recCfg.bolCorrConcurrent &&
                        this.bolCorrStrm==recCfg.bolCorrStreaming;
            }
            
            public boolean  isIndex(Index index) {
                return this.cntMaxStreams==index.cntMaxStreams &&
                        this.bolCorrCon==index.bolCorrCon &&
                        this.bolCorrStrm==index.bolCorrStrm;
            }
        }

        /**
         * Internal score given to <code>ConfigResult</code> records.
         */
        private static class Score /* implements Comparable<Score> */ {
            
            /** Creator */
            private static Score from(Index recIndex) {
                return new Score(recIndex);
            }

            /**
             * <p>
             * Provides reverse ordering of <code>Score</code> objects by data rate.
             * </p>
             */
            private static Comparator<Score>    createRateOrdering() {
                Comparator<Score>   cmp = (o1, o2) -> {
                    if (o1.dblRateAvg > o2.dblRateAvg)
                        return -1;
//                    // Must avoid collisions
//                    else if (o1.dblScore == o2.dblScore)
//                        return 0;
                    else
                        return +1;
                };
                
                return cmp;
            }
            
            /**
             * <p>
             * Provides reverse ordering of <code>Score</code> objects by hit count.
             * </p>
             */
            private static Comparator<Score>    createHitOrdering() {
                Comparator<Score>   cmp = (o1, o2) -> {
                    if (o1.cntHitsTot > o2.cntHitsTot)
                        return -1;
//                    // Must avoid collisions
//                    else if (o1.cntHits == o2.cntHits)
//                        return 0;
                    else 
                        return +1;
                };
                
                return cmp;
            }
            
            //
            // Score Attributes
            //
            public  final Index   recIndex;
            
            public  double  dblRateAvg = 0.0; 
            public  double  dblRateMin = 0.0;
            public  double  dblRateMax = 0.0;
            
            public  int     cntHitsTot = 0; 
            public  int     cntHalfSrc = 0;
            public  int     cntHalfRng = 0;
            
            public Score(Index recIndex) {
                this.recIndex = recIndex;
            }

//            /**
//             * <p>
//             * Provides reverse ordering of <code>Score</code> objects by hit count.
//             * </p>
//             *
//             * @see java.lang.Comparable#compareTo(java.lang.Object)
//             */
//            @Override
//            public int compareTo(Score o) {
//                if (this.cntHits > o.cntHits)
//                    return -1;
//                else if (this.cntHits == o.cntHits)
//                    return 0;
//                else return +1;
//            }
        }

        //
        // Class Constants
        //
        
        /** Maximum number of gRPC data streams - Make sure to set to the the correct value! */
        private static final int        CNT_MAX_STREAMS = 5;
        
        /** List of boolean values used for configuration */
        private static List<Boolean>    LST_BOOLS = List.of(false, true);

        //
        // Class Resources
        //

        /** List of all possible indexes for the given <code>{@link #CNT_MAX_STREAMS}</code> - initialized in static section */
        private static final List<Index>    LST_INDEXES = new LinkedList<>();

        
        /*
         * Initialize the class constants
         */
        static {
            LST_INDEXES.clear();
            
            for (int cntStrms=1; cntStrms<=CNT_MAX_STREAMS; cntStrms++) 
                for (Boolean bolCorrCon : LST_BOOLS)
                    for (Boolean bolCorrStrm : LST_BOOLS) {
                        Index   index = Index.from(cntStrms, bolCorrCon, bolCorrStrm);
                        
                        LST_INDEXES.add(index);
                    }
        }
        
        /**
         * <p>
         * Gets the internal index from the given <code>TestConfig</code record.
         * 
         * @param recCfg    configuration record to cross reference
         * 
         * @return  <code>Index</code> record for the given configuration
         */
        public static Index    indexFrom(TestConfig recCfg) {
            for (Index index : LST_INDEXES) {
                if (index.isIndex(recCfg))
                    return index;
            }
            
            return Index.NULL_INDEX;
        }
        
        //
        // Instance Resources
        //

        /** Map of indexes to result scores */
        private Map<Index, Score>  mapScore = new HashMap<>();
        
        /**
         * <p>
         * Constructs a new instance of <code>ConfigResultScore</code>.
         * </p>
         */
        public ConfigResultScore() {
        }
        
        //
        // Operations
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
            Index   recIndex = ConfigResultScore.indexFrom(recResult.recConfig);
            
            // Get the current score record for the index - make new score if none exists
            Score   recScore = this.mapScore.get(recIndex);
            
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
            Optional<Score> optScore = this.mapScore
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
            Index   recIndex = ConfigResultScore.indexFrom(recResult.recConfig);
            
            // Get the current score record for the index - make new score if none exists
            Score   recScore = this.mapScore.get(recIndex);
            
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
            for (Score score : this.mapScore.values()) {
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
            for (Score score : this.mapScore.values()) {
                setScores.add(score);
            }
            
            return setScores;
        }
        
        
//        /**
//         * @param recCfg    desired configuration
//         * @param dblValue  value to add to configuration score
//         * 
//         * @return  new value of configuration score
//         */
//        public double   addScore(ConfigResult recResult) {
//            
//            // Get the index for the configuration w/in the result
//            Index   recIndex = ConfigResultScore.indexFrom(recResult.recConfig);
//            
//            // Get the current score record for the index - make new score if none exists
//            Score   recScore = this.mapScore.get(recIndex);
//            
//            if (recScore == null) {
//                recScore = Score.from(recIndex);
//                
//                this.mapScore.put(recIndex, recScore);
//            }
//            
//            // Add in the scores
//            recScore.dblScore += recResult.dblRate;
//            recScore.cntHits++;
//            
//            if (recResult.request.equals(RQST_HALF_SRC))
//                recScore.cntHalfSrc++;
//            if (recResult.request.equals(RQST_HALF_RNG))
//                recScore.cntHalfRng++;
//            
//            return recScore.dblScore;
//        }
        
        /**
         * <p>
         * Scores the given result record and saves the scoring results into the collection of
         * scoring based upon configuration.
         * </p>
         * <p>
         * Here the running score for a configuration (by <code>Index</code>) is the average value
         * of the <code>{@link ConfigResult#dblRate}</code> field.
         * Note that scoring also includes the various hit counts for the configuration (i.e., <code>Index</code>)
         * and other <code>ConfigResult</code> parameters.
         * </p>
         * 
         * @param recCfg    the configuration result to be scored (containing configuration record)
         * @param dblValue  value to average into the current configuration score
         * 
         * @return  new average value of the configuration score
         */
        public double   score(ConfigResult recResult) {
            
            // Get the index for the configuration w/in the result
            Index   recIndex = ConfigResultScore.indexFrom(recResult.recConfig);
            
            // Get the current score record for the index - make new score if none exists
            Score   recScore = this.mapScore.get(recIndex);
            
            if (recScore == null) { 
                recScore = Score.from(recIndex);
                
                this.mapScore.put(recIndex, recScore);
            }
            
            // Average in the scores
            recScore.dblRateAvg *= recScore.cntHitsTot;
            recScore.dblRateAvg += recResult.dblRate;
            recScore.cntHitsTot++;
            recScore.dblRateAvg /= recScore.cntHitsTot;
            
            // Record min/max values
            if (recResult.dblRate > recScore.dblRateMax)
                    recScore.dblRateMax = recResult.dblRate;
            if (recScore.dblRateMin == 0.0)
                recScore.dblRateMin = recResult.dblRate;
            if (recResult.dblRate < recScore.dblRateMin)
                recScore.dblRateMin = recResult.dblRate;
            
            // Record hit count
            if (recResult.request.equals(RQST_HALF_SRC))
                recScore.cntHalfSrc++;
            if (recResult.request.equals(RQST_HALF_RNG))
                recScore.cntHalfRng++;
            
            return recScore.dblRateAvg;
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

            this.printOut(ps, setScores);
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

            this.printOut(ps, setScores);
        }
        
        private void printOut(PrintStream ps, SortedSet<Score> setScores) {
            
            ps.println("ConfigResult Scoring");
            ps.println("  Total number of results = " + setScores.size());
            ps.println("  Total number of hits    = " + setScores.stream().mapToInt(rec -> rec.cntHitsTot).sum());
            ps.println("  Total average data rate = " + setScores.stream().mapToDouble(rec -> rec.dblRateAvg).sum()/setScores.size());
            for (Score score : setScores) {
                ps.print("    Hits=" + score.cntHitsTot + ", Avg Rate=" + score.dblRateAvg + ", Min Rate=" + score.dblRateMin + ", Max Rate=" + score.dblRateMax + ": "); 

                ps.print("HALF_SRC count=" + score.cntHalfSrc + ", HALF_RNG count=" + score.cntHalfRng + ", ");

                ps.print("Config={Max streams=" + score.recIndex.cntMaxStreams);
                ps.print(", correl conc=" + score.recIndex.bolCorrCon);
                ps.print(", correl stream=" + score.recIndex.bolCorrStrm);
                ps.print("}, ");
                
                ps.println();
            }
        }
    }

    
    /**
     * <p>
     * Record containing all the parameters of a time-series data request explicit multi-streaming data recovery test.
     * </p>
     * 
     * @param   rqstOrg     the original time-series data request
     * @param   enmDcmpType the request domain decomposition type
     * @param   enmStrType  the gRPC data stream type used to recover the request data
     * @param   cntStrms    the number of gRPC streams used for request data recovery
     * @param   lstCmpRqsts the resulting composite time-series data request (from creator)
     *
     * @author Christopher K. Allen
     * @since Jan 18, 2025
     *
     */
    public static record    TestCase(DpDataRequest rqstOrg, RequestDecompType enmDcmpType, DpGrpcStreamType enmStrmType, int cntStrms, List<DpDataRequest> lstCmpRqsts) {
        
        /** Request decomposer used in non-canonical construction/creation */
        private static final    DataRequestDecomposer   PRCR_DECOMP = DataRequestDecomposer.create();
        
        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates a new instances of <code>TestCase</code> from the given parameters.
         * </p>
         * 
         * @param   rqstOrg     the original time-series data request
         * @param   enmDcmpType the request domain decomposition type
         * @param   enmStrType  the gRPC data stream type used to recover the request data
         * @param   cntStrms    the number of gRPC streams used for request data recovery
         * @param   lstCmpRqsts    the resulting composite time-series data request
         *
         * @return  a new, initialized instance of <code>TestCase</code>
         */
        public static TestCase  from(DpDataRequest rqstOrg, RequestDecompType enmRqstDcmp, DpGrpcStreamType enmStrmType, int cntStrms) {
        
            List<DpDataRequest> lstRqsts = PRCR_DECOMP.buildCompositeRequest(rqstOrg, enmRqstDcmp, cntStrms);
            
            lstRqsts.forEach(r -> r.setStreamType(enmStrmType));
            
            return new TestCase(rqstOrg, enmRqstDcmp, enmStrmType, cntStrms, lstRqsts);
        }
    }
    

    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/impl/";
    
    
    /** Large test request */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_SRC = TestQueryResponses.QREC_HALF_SRC.createRequest();

    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_RNG = TestQueryResponses.QREC_HALF_RNG.createRequest();

    /** The data request for the entire test archive - used for timing responses */
    public static final DpDataRequest   RQST_ALL = TestQueryResponses.QREC_ALL.createRequest();

    
    /** Common List of data requests used for testing */
    public static final List<DpDataRequest>         LST_RQSTS = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
    
    /** A map of the common test requests to their (arbitrary) names */
    public static final Map<DpDataRequest, String>  MAP_RQST_NMS = Map.of(RQST_HALF_SRC, "RQST_HALF_SRC", RQST_HALF_RNG, "RQST_HALF_RNG");
    
    
    /** Configuration - List of all maximum data stream counts for request recovery */
    public static final List<Integer>           LST_CFG_MAX_STRMS = List.of(1, 2, 3, 4, 5);
//    public static final List<Integer>           LST_CFG_MAX_STRMS = List.of(2, 3);
    
    /** Configuration - List of all maximum data source counts for composite requests */
    public static final List<Integer>           LST_CFG_MAX_SRCS = List.of(500, 1000, 2000, 4000);
//    public static final List<Integer>           LST_CFG_MAX_SRCS = List.of(2000);
    
    /** Configuration - List of maximum time range duration for composite requests */
    public static final List<Duration>          LST_CFG_MAX_DUR = List.of(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L), Duration.ofSeconds(5L));
//    public static final List<Duration>          LST_CFG_MAX_DUR = List.of(Duration.ofSeconds(5L));
    
    /** Configuration - List of all common configuration records - built in static section */
    public static final List<TestConfig>        LST_CFG_TEST_CASES = new LinkedList<>();
    
//    /** Configuration - Sorted set of all configuration results */
//    public static final SortedSet<ConfigResult> SET_CFG_RSLTS = new TreeSet<>(ConfigResult.ResultOrder.create());
    
    
    /** Test Case - List of multi-streaming gRPC data streams */
    public static final List<Integer>           LST_MSTRM_CNT_STRMS = List.of(1, 2, 3, 4, 5);
    
    /** Test Case - List of gRPC data stream types used for request recovery */
    public static final List<DpGrpcStreamType>  LST_MSTRM_STRM_TYP = List.of(DpGrpcStreamType.BACKWARD, DpGrpcStreamType.BIDIRECTIONAL);

    /** Test Case - List of request domain decomposition strategies */
    public static final List<RequestDecompType> LST__MSTRM_DCMP_TYP = List.of(RequestDecompType.HORIZONTAL, RequestDecompType.VERTICAL);
    
    /** Test Case - List of all common test case records - built in static section */
    public static final List<TestCase>          LST_MSTRM_TST_CASES = new LinkedList<>();
    
    
    /** Universal enable/disable recovery request with multiple gRPC data streams flag */
    public static final boolean                 BOL_UNIV_RCVR_MULT = true;
    
    /** Universal enable/disable correlation concurrency flag */
    public static final boolean                 BOL_UNIVR_CORR_CONC = true;
    
    /** Universal enable/disable correlation while streaming flag */
    public static final boolean                 BOL_UNIVR_CORR_STRM = true;
    
    
    /*
     * Static block - Initialize configuration and multi-stream test cases
     */
    static {
        List<Boolean>   lstBool = List.of(false, true);
        
        // Populate the test configurations
        for (Integer cntMaxStrms : LST_CFG_MAX_STRMS)
            for (Integer cntMaxSrcs : LST_CFG_MAX_SRCS)
                for (Duration durMaxRng : LST_CFG_MAX_DUR)
                    for (Boolean bolCorrCon : lstBool) 
                        for (Boolean bolCorrStrm : lstBool) {

                            TestConfig  recCfg = TestConfig.from(bolCorrCon, bolCorrStrm, cntMaxStrms, cntMaxSrcs, durMaxRng);

                            LST_CFG_TEST_CASES.add(recCfg);
                        }
        
        // Populate the test cases, building the composite request
        for (DpDataRequest rqst : LST_RQSTS)
            for (DpGrpcStreamType enmStrType : LST_MSTRM_STRM_TYP)
                for (RequestDecompType enmDcmpType : LST__MSTRM_DCMP_TYP)
                    for (Integer cntStrms : LST_MSTRM_CNT_STRMS) {
                        
                        TestCase    recCase = TestCase.from(rqst, enmDcmpType, enmStrType, cntStrms);
                        
                        LST_MSTRM_TST_CASES.add(recCase);
                    }
    }
    
    
    //
    // Test Fixture Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection        connQuery;
    
    /** The QueryRequestProcessor under test */
    private static QueryRequestProcessor   prcrQuery;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream              psOutput;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Query Service connection
        connQuery = DpQueryConnectionFactory.FACTORY.connect();
        
        // Create the test query response processor
        prcrQuery = QueryRequestProcessor.from(connQuery);
        
        // Open the common output file
        String  strFileName = QueryRequestProcessorTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(QueryRequestProcessorTest.class.getSimpleName() + ": " + strDateTime);
        psOutput.println("  Multi-streaming request recovery : " + BOL_UNIV_RCVR_MULT);
        psOutput.println("  Correlate using multiple threads : " + BOL_UNIVR_CORR_CONC);
        psOutput.println("  Correlate while streaming        : " + BOL_UNIVR_CORR_STRM);
        psOutput.println();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connQuery.shutdownSoft();
        psOutput.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    
    //
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testFrom() {
        QueryRequestProcessor  prcrTest = QueryRequestProcessor.from(connQuery);
        
        Assert.assertNotNull(prcrTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#QueryResponseProcessor(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testQueryResponseProcessor() {
        QueryRequestProcessor  prcrTest = new QueryRequestProcessor(connQuery);
        
        Assert.assertNotNull(prcrTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#enableMultiStreaming(boolean)}.
     */
    @Test
    public final void testEnableMultiStreaming() {
        boolean bolEnabled = prcrQuery.isMultiStreaming();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming enabled: " + bolEnabled);
        
        prcrQuery.enableMultiStreaming(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isMultiStreaming() != bolEnabled);
        
        prcrQuery.enableMultiStreaming(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#setMultiStreamingDomainSize(long)}.
     */
    @Test
    public final void testSetMultiStreamingDomainSize() {
        long    lngDomSize = prcrQuery.getMultiStreamingDomainSize();
        long    lngNewSize = lngDomSize/2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming domain size: " + lngDomSize);
        
        prcrQuery.setMultiStreamingDomainSize(lngNewSize);
        
        Assert.assertEquals(lngNewSize, prcrQuery.getMultiStreamingDomainSize());
     
        prcrQuery.setMultiStreamingDomainSize(lngDomSize);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#setMaxStreamCount(int)}.
     */
    @Test
    public final void testSetMaxStreamCount() {
        int     cntMaxStrs = prcrQuery.getMaxStreamCount();
        int     cntNewStrs = cntMaxStrs * 2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming maximum stream count: " + cntMaxStrs);
        
        prcrQuery.setMaxStreamCount(cntNewStrs);
        
        Assert.assertEquals(cntNewStrs, prcrQuery.getMaxStreamCount());
        
        prcrQuery.setMaxStreamCount(cntMaxStrs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#setMaxDataSourceCount(int)}.
     */
    @Test
    public final void testSetMaxDataSourceCount() {
        int     cntMaxSrcs = prcrQuery.getMaxDataSourceCount();
        int     cntNewSrcs = cntMaxSrcs * 2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default maximum data source count: " + cntMaxSrcs);
        
        prcrQuery.setMaxStreamCount(cntNewSrcs);
        
        Assert.assertEquals(cntNewSrcs, prcrQuery.getMaxStreamCount());
        
        prcrQuery.setMaxStreamCount(cntMaxSrcs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#setMaxTimeRange(java.time.Duration)}.
     */
    @Test
    public final void testSetMaxTimeRange() {
        Duration    durMaxRng = prcrQuery.getMaxTimeRange();
        Duration    durNewRng = durMaxRng.dividedBy(2L);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default maximum time range duration: " + durMaxRng);
        
        prcrQuery.setMaxTimeRange(durNewRng);
        
        Assert.assertEquals(durNewRng, prcrQuery.getMaxTimeRange());
        
        prcrQuery.setMaxTimeRange(durMaxRng);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#enableCorrelateConcurrently(boolean)}.
     */
    @Test
    public final void testEnableCorrelateConcurrently() {
        boolean bolEnabled = prcrQuery.isCorrelatingConcurrently();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default correlate concurrently: " + bolEnabled);
        
        prcrQuery.enableCorrelateConcurrently(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isCorrelatingConcurrently() != bolEnabled);
        
        prcrQuery.enableCorrelateConcurrently(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#enableCorrelateWhileStreaming(boolean)}.
     */
    @Test
    public final void testEnableCorrelateWhileStreaming() {
        boolean bolEnabled = prcrQuery.isCorrelatingWhileStreaming();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default correlate while streaming: " + bolEnabled);
        
        prcrQuery.enableCorrelateWhileStreaming(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isCorrelatingWhileStreaming() != bolEnabled);
        
        prcrQuery.enableCorrelateWhileStreaming(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#processRequest(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequest() {
        
        // Test Parameters
        final PrintStream   os = psOutput;
        final boolean       bolMultStrm = BOL_UNIV_RCVR_MULT;
        final int           cntCases = 2*LST_CFG_TEST_CASES.size();  
        
        // Test resource - we are changing configuration so we use an independent processor
        final QueryRequestProcessor    prcrTest = QueryRequestProcessor.from(connQuery);
        final SortedSet<ConfigResult>   setResults = new TreeSet<>(ConfigResult.ResultOrder.create());
        
        prcrTest.enableMultiStreaming(bolMultStrm);
        
        // Print Header
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        os.println();
        
        int     indCase = 1;
        for (TestConfig recCfg : LST_CFG_TEST_CASES) { 
            
            // Configure the processor
            recCfg.configure(prcrTest);
            
            for (DpDataRequest rqst : LST_RQSTS) {
                String strRqstNm = MAP_RQST_NMS.get(rqst);

                System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Running Test Case #" + indCase + " of " + cntCases + " for request " + strRqstNm);
                try {

                    Instant insStart = Instant.now();
                    SortedSet<CorrelatedQueryData>  setData = prcrTest.processRequest(rqst);
                    Instant insFinish = Instant.now();

                    // Compute results
                    Duration    durRqst = Duration.between(insStart, insFinish);
                    int         cntMsgs = prcrTest.getProcessedMessageCount();
                    int         cntBlks = setData.size();
                    long        szAlloc = prcrTest.getProcessedByteCount();
                    List<DpDataRequest> lstCmpRqsts = prcrTest.getProcessedCompositeRequest();

                    double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durRqst.toNanos();

                    // Create result record and save
                    ConfigResult    recResult = ConfigResult.from(dblRateXmit, indCase, cntMsgs, szAlloc, cntBlks, durRqst, recCfg, rqst, strRqstNm, lstCmpRqsts);
                    setResults.add(recResult);

                } catch (DpQueryException e) {
                    Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during test case #" + indCase + " : " + e.getMessage());
                }

                indCase++;
            }
        }
        
        // Deal with the set of configuration results
        this.dealWithProcessRequestResults(os, setResults);
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.QueryRequestProcessor#processRequests(java.util.List)}.
     */
    @Test
    public final void testProcessRequests() {
        
        // Test Parameters
        final PrintStream   os = psOutput;
        final boolean       bolMultStrm = BOL_UNIV_RCVR_MULT;
        final boolean       bolCorrCon = BOL_UNIVR_CORR_CONC;    
        final boolean       bolCorrStrm = BOL_UNIVR_CORR_STRM;        
        
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        os.println();
        
        // Configure Correlator
        QueryRequestProcessorTest.prcrQuery.enableMultiStreaming(bolMultStrm);
        QueryRequestProcessorTest.prcrQuery.enableCorrelateConcurrently(bolCorrCon);
        QueryRequestProcessorTest.prcrQuery.enableCorrelateWhileStreaming(bolCorrStrm);
        
        int     indCase = 1;
        for (TestCase recCase : LST_MSTRM_TST_CASES) {
        
            os.println("Test Case #" + indCase);
            try {
                
                Instant insStart = Instant.now();
                SortedSet<CorrelatedQueryData>  setData = QueryRequestProcessorTest.prcrQuery.processRequests(recCase.lstCmpRqsts);
                Instant insFinish = Instant.now();
                
                // Compute results
                Duration    durRqst = Duration.between(insStart, insFinish);
                int         cntMsgs = QueryRequestProcessorTest.prcrQuery.getProcessedMessageCount();
                int         cntBlks = setData.size();
                long        szAlloc = QueryRequestProcessorTest.prcrQuery.getProcessedByteCount();
                
                double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durRqst.toNanos();
                
                // Print out results
                os.println("  Recover w/ multi-stream : " + bolMultStrm);
                os.println("  Correlate w/ streaming  : " + bolCorrStrm);
                os.println("  Correlation concurrency : " + bolCorrCon);
                os.println("  Messages recovered      : " + cntMsgs);
                os.println("  Processed data blocks   : " + cntBlks);
                os.println("  Processed bytes         : " + szAlloc);
                os.println("  Request duration        : " + durRqst);
                os.println("  Processing rate (Mbps)  : " + dblRateXmit);
                this.printConfiguration(os, recCase);
                os.println();
                
            } catch (DpQueryException e) {
                Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request recovery #" + indCase + " : " + e.getMessage());
            }
            
            indCase++;
        }
    }
    
    
    // 
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the <code>ConfigResult</code> scoring and prints out the scoring and results.
     * </p>
     * 
     * @param os            print stream to receive results output
     * @param setResults    the set of results from <code>{@link QueryRequestProcessor#processRequest(DpDataRequest)}</code>
     */
    private void    dealWithProcessRequestResults(PrintStream os, final SortedSet<ConfigResult>   setResults) {
        
        // Compute general results and print them
        int         cntResults = setResults.size();
        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblRate).sum()/cntResults;
        int         cntRatesAboveAvg = setResults.stream().filter(rec -> rec.dblRate >= dblRateAvg).mapToInt(rec -> 1).sum();
        int         cntRatesAbove200 = setResults.stream().filter(rec -> rec.dblRate >= 200.0).mapToInt(rec -> 1).sum();
        Duration    durRqstAvg = setResults.stream().<Duration>map(rec -> rec.durRequest).reduce(Duration.ZERO, (r1,r2) -> r1.plus(r2)).dividedBy(cntResults);
        
        int         cntHalfSrc = 0;
        int         cntHalfRng = 0;
        for (ConfigResult recResult : setResults) {
            if (recResult.request.equals(RQST_HALF_SRC))
                cntHalfSrc++;
            if (recResult.request.equals(RQST_HALF_RNG))
                cntHalfRng++;
        }
        
        Double  dblRateMax = null;
        Double  dblRateMin = null;
        Double  dblRateStd = 0.0;
        for (ConfigResult recResult : setResults) {
            double  dblRate = recResult.dblRate;
            
            if (dblRateMax == null)
                dblRateMax = dblRate;
            if (dblRateMin == null)
                dblRateMin = dblRate;
            if (dblRate > dblRateMax)
                dblRateMax = dblRate;
            if (dblRate < dblRateMin)
                dblRateMin = dblRate;
            
            dblRateStd += (dblRate - dblRateAvg)*(dblRate - dblRateAvg);
        }
        dblRateStd = Math.sqrt(dblRateStd/cntResults);
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results Average Scoring:");
        os.println("    Total number of result cases : " + cntResults);
        os.println("    Cases with rates >= Avg Rate : " + cntRatesAboveAvg);
        os.println("    Cases with rates >= 200 Mbps : " + cntRatesAbove200);
        os.println("    Average data rate (Mbps)     : " + dblRateAvg);
        os.println("    Minimum data rate (Mbps)     : " + dblRateMin);
        os.println("    Maximum data rate (Mbps)     : " + dblRateMax);
        os.println("    Standard deviation (Mbps)    : " + dblRateStd);
        os.println("    Average request duration     : " + durRqstAvg);
        os.println("    Number of HALF_SRC requests  : " + cntHalfSrc);
        os.println("    Number of HALF_RNG requests  : " + cntHalfRng);
        
        // Score results and print them
        ConfigResultScore   scoreAll = new ConfigResultScore();
        ConfigResultScore   scoreAboveAvg = new ConfigResultScore();
        ConfigResultScore   scoreAbove200 = new ConfigResultScore();
        int     cntAboveAvg = 0;
        int     cntAbove200 = 0;
        int     cntBoth = 0;
        boolean bolTag = false;
        
        for (ConfigResult recResult : setResults) {
            bolTag = false;
            
            // Cases with rates >= the average rate
            if (recResult.dblRate >= dblRateAvg) {
                scoreAboveAvg.score(recResult);
                cntAboveAvg++;
                bolTag = true;
            }
            
            // Cases with rates >= 200 Mbps
            if (recResult.dblRate >= 200.0) {
                scoreAbove200.score(recResult);
                cntAbove200++;
                if (bolTag == true)
                    cntBoth++;
            }
            
            // All cases
            scoreAll.score(recResult);
        }
        
        os.println();
        os.println("    Parsed cases over average    : " + cntAboveAvg);
        os.println("    Parsed cases  over 200 Mbps  : " + cntAbove200);
        os.println("    Parsed both over avg and 200 : " + cntBoth);
        
        ConfigResultScore.Score scoreBest = scoreAll.getScoreWithBestAvgRate();
        double                  dblRateMinBestConfig = scoreBest.dblRateMin;
        ConfigResultScore       scoreBestAboveMin = new ConfigResultScore();
        
        for (ConfigResult recResult : setResults) {
            if (recResult.dblRate >= dblRateMinBestConfig)
                scoreBestAboveMin.score(recResult);
        }
        
        os.println("    Minimum rate for best config : " + dblRateMinBestConfig);
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results >= " + dblRateMinBestConfig + " Mbps Scoring:");
        scoreBestAboveMin.printOutByHits(os);
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results >= 200 Mbps Scoring:");
        scoreAbove200.printOutByHits(os);
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results >= " + dblRateAvg + " Mbps Scoring:");
        scoreAboveAvg.printOutByHits(os);
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results All Scoring:");
        scoreAll.printOutByRates(os);
        
        // Printout results
        int     indResult = 1;
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " RESULTS:");
        os.println("  Total number of test cases run: " + setResults.size());
        os.println();
        for (ConfigResult recResult : setResults) {
            os.println("  Result #" + indResult);
            recResult.printOut(os, "  ");
            os.println();
            
            indResult++;
        }
    }
    
    /**
     * <p>
     * Prints out the configuration of the given test case record to the given print stream.
     * </p>
     * 
     * @param os        print stream receiving output data (i.e., the configuration)
     * @param recCase   the test case whose configuration is to be output 
     */
    private void    printConfiguration(PrintStream os, TestCase recCase) {
        
        DpDataRequest   rqst = recCase.rqstOrg;
        
        os.println("  Original Time-series Data Request");
        this.printConfiguration(os, "", rqst);
        
        os.println("  Query Recovery Parameters");
        os.println("    Request decomposition type : " + recCase.enmDcmpType);
        os.println("    gRPC stream type           : " + recCase.enmStrmType);
        os.println("    Number of gRPC streams     : " + recCase.cntStrms);
        
        int     indRqst = 1;
        for (DpDataRequest rqstCmp : recCase.lstCmpRqsts) {
            os.println("    Composite Request #" + indRqst);
            this.printConfiguration(os, "  ", rqstCmp);
            indRqst++;
        }
    }
    
    /**
     * <p>
     * Prints out the configuration of the given data request to the given print stream.
     * </p>
     * 
     * @param os        print stream receiving output data (i.e., the configuration)
     * @param strPad    padding string for parameters
     * @param rqst      the data request whose configuration is to be output 
     */
    private void    printConfiguration(PrintStream os, String strPad, DpDataRequest rqst) {
        
        os.println(strPad + "    gRPC stream type   : " + rqst.getStreamType());
        os.println(strPad + "    data source count  : " + rqst.getSourceCount());
        os.println(strPad + "    duration (seconds) : " + rqst.rangeDuration().toSeconds());
        os.println(strPad + "    time interval      : " + rqst.range());
        os.println(strPad + "    domain size        : " + rqst.approxDomainSize());
    }


}
