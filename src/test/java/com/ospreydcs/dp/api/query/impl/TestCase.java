package com.ospreydcs.dp.api.query.impl;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryDataOld;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;

/**
     * <p>
     * Record containing all the parameters of a time-series data request explicit multi-streaming data recovery test.
     * </p>
     * <p>
     * This record is intended for evaluation of request processors for the multi-streaming operation 
     * <code>{@link QueryRequestProcessorNew#processRequests(List)}</code>.  Specifically, it holds the
     * original request, the desired gRPC stream type, the desired number of gRPC data streams, and the
     * request decomposition strategy.
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
    public record    TestCase(
            DpDataRequest rqstOrg, 
            RequestDecompType enmDcmpType, 
            DpGrpcStreamType enmStrmType, 
            int cntStrms,
            List<DpDataRequest> lstCmpRqsts
            ) 
    {
        
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
        public static TestCase  from(DpDataRequest rqstOrg, 
                RequestDecompType enmRqstDcmp, 
                DpGrpcStreamType enmStrmType, 
                int cntStrms) 
        {
            // Decompose the original request
            String              strRqstId = rqstOrg.getRequestId();
            List<DpDataRequest> lstRqsts = PRCR_DECOMP.buildCompositeRequest(rqstOrg, enmRqstDcmp, cntStrms);
            
            // Assign parameters of the composite requests
            lstRqsts.forEach(r -> r.setStreamType(enmStrmType));
            lstRqsts.forEach(r -> r.setRequestId(strRqstId));
            
            return new TestCase(rqstOrg, enmRqstDcmp, enmStrmType, cntStrms, lstRqsts);
        }
        
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Performs the multi-streamed, time-series data request on the given processor for the given configuration and evaluates results.
         * </p>
         * <p>
         * First the given <code>TestConfig</code> record is used to configure the given <code>QueryRequestProcessorNew</code> instance.
         * Then the given <code>DpDataRequest</code> is used for the <code>{@link QueryRequestProcessorNew#processRequest(DpDataRequest)</code>
         * operation. 
         * The performance of the operation is measured and returned in the <code>ConfigResult</code> record.
         * </p>
         * 
         * @param prcrRqsts     the request processor under evaluation
         * @param recPrcrCfg    configuration record for the given request processor
         * 
         * @return  a new <code>ConfigResult</code> record containing the results of the processor request evaluation
         * 
         * @throws DpQueryException general Query Service exception during request processing (see message and cause)
         */
        public ConfigResult  evaluate(QueryRequestProcessorNew prcrRqsts, TestConfig recPrcrCfg) throws DpQueryException {

            // Configure the processor
            recPrcrCfg.configure(prcrRqsts);

            Instant insStart = Instant.now();
            SortedSet<RawCorrelatedData>  setData = prcrRqsts.processRequests(this.lstCmpRqsts);
            Instant insFinish = Instant.now();

            // Compute results
            Duration    durRqst = Duration.between(insStart, insFinish);
            int         cntMsgs = prcrRqsts.getProcessedMessageCount();
            int         cntBlks = setData.size();
            long        szAlloc = prcrRqsts.getProcessedByteCount();

            double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durRqst.toNanos();

            // Create result record and save
            ConfigResult    recResult = ConfigResult.from(dblRateXmit, cntMsgs, szAlloc, cntBlks, durRqst, recPrcrCfg, this.rqstOrg, this.lstCmpRqsts);

            return recResult;
        }
        
        /**
         * <p>
         * Performs the multi-streamed, time-series data request on the given processor for the given configuration and evaluates results.
         * </p>
         * <p>
         * First the given <code>TestConfig</code> record is used to configure the given <code>QueryRequestProcessorOld</code> instance.
         * Then the given <code>DpDataRequest</code> is used for the <code>{@link QueryRequestProcessorOld#processRequest(DpDataRequest)</code>
         * operation. 
         * The performance of the operation is measured and returned in the <code>ConfigResult</code> record.
         * </p>
         * 
         * @param prcrRqsts     the request processor under evaluation
         * @param recPrcrCfg    configuration record for the given request processor
         * 
         * @return  a new <code>ConfigResult</code> record containing the results of the processor request evaluation
         * 
         * @throws DpQueryException general Query Service exception during request processing (see message and cause)
         */
        @SuppressWarnings("deprecation")
        public ConfigResult  evaluate(QueryRequestProcessorOld prcrRqsts, TestConfig recPrcrCfg) throws DpQueryException {

            // Configure the processor
            recPrcrCfg.configure(prcrRqsts);

            Instant insStart = Instant.now();
            SortedSet<CorrelatedQueryDataOld>  setData = prcrRqsts.processRequests(this.lstCmpRqsts);
            Instant insFinish = Instant.now();

            // Compute results
            Duration    durRqst = Duration.between(insStart, insFinish);
            int         cntMsgs = prcrRqsts.getProcessedMessageCount();
            int         cntBlks = setData.size();
            long        szAlloc = prcrRqsts.getProcessedByteCount();

            double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durRqst.toNanos();

            // Create result record and save
            ConfigResult    recResult = ConfigResult.from(dblRateXmit, cntMsgs, szAlloc, cntBlks, durRqst, recPrcrCfg, this.rqstOrg, this.lstCmpRqsts);

            return recResult;
        }
        
        /**
         * <p>
         * Prints out the configuration of the this test case record to the given print stream.
         * </p>
         * 
         * @param ps        print stream receiving output data (i.e., the configuration)
         */
        public void    printConfiguration(PrintStream ps) {

            // Print out the decomposition  parameters
            ps.println("  Request Decomposition Parameters");
            ps.println("    Request decomposition type : " + this.enmDcmpType);
            ps.println("    gRPC stream type           : " + this.enmStrmType);
            ps.println("    Number of gRPC streams     : " + this.cntStrms);
            
//            ps.println("  Processor Configuration");
//            this.recConfig.printOut(ps, "    ");
//            
            // Print out original  request properties
            ps.println("  Original Time-series Data Request");
            this.printConfiguration(ps, "", this.rqstOrg);

            // Print out the composite requests properties
            int     indRqst = 1;
            for (DpDataRequest rqstCmp : this.lstCmpRqsts) {
                ps.println("    Composite Request #" + indRqst);
                this.printConfiguration(ps, "  ", rqstCmp);
                indRqst++;
            }
        }
        
        //
        // Support Methods
        //
        
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
            os.println(strPad + "    Request identifier : " + rqst.getRequestId());
            os.println(strPad + "    gRPC stream type   : " + rqst.getStreamType());
            os.println(strPad + "    Data source count  : " + rqst.getSourceCount());
            os.println(strPad + "    Duration (nsecs)   : " + rqst.rangeDuration().toNanos());
            os.println(strPad + "    Time interval      : " + rqst.range());
            os.println(strPad + "    Domain size        : " + rqst.approxDomainSize());
        }

    }