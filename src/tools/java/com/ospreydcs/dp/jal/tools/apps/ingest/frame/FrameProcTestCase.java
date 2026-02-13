/*
 * Project: dp-jal
 * File:	FrameProcTestCase.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcTestCase
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
 * @since Sep 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.io.PrintStream;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing the parameters for a single <code>FrameProcessorEvaluator</code> application test case.
 * </p>
 * <p>
 * The record contains parameters for both the configuration for a payload of <code>IngestionFrame</code> objects
 * and the configuration for the <code>IngestionFrameProcessor</code> instance under evaluation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 13, 2025
 *
 * @param   indCase     test case index
 * 
 * @param   cntFrame    number of <code>IngestionFrame</code> instances composing test payload
 * @param   specFrame   specification record for <code>IngestionFrame</code> factory
 * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
 *
 * @author Christopher K. Allen
 * @since Sep 15, 2025
 *
 */
public record FrameProcTestCase(
        int                     indCase,
        int                     cntFrames,
        FrameFactorySpec        specFrame,
        FrameProcessorConfig    recPrcrCfg
        ) implements Comparable<FrameProcTestCase>
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>FrameProcTestCase</code> record populated from the given arguments.
     * </p>
     * <p>
     * The <code>{@link #indCase}</code> field is assigned with the current value of <code>{@link #IND_CASE}</code>
     * which is allows incremented in the canonical constructor.
     * </p>
     * 
     * @param   cntFrame    number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   configuration record for <code>IngestionFrame</code> builder
     * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     * 
     * @return  a new <code>FrameProcTestCase</code> record with fields populated with the given arguments
     */
    public static FrameProcTestCase from(int cntFrames, FrameFactorySpec specFrame, FrameProcessorConfig recPrcrCfg) {
        return new FrameProcTestCase(IND_CASE, cntFrames, specFrame, recPrcrCfg);
    }

    
    //
    // Record Resources
    //
    
    /** Running index of test case - incremented upon creation/construction */
    private static int  IND_CASE = 1;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor. Sets field values then increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param   indCase     test case index
     * 
     * @param   cntFrame    number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   specification record for <code>IngestionFrame</code> factory
     * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     */
    public FrameProcTestCase {
        IND_CASE++;
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> providing a natural (ascending) ordering according to the test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the index field within two
     * <code>FrameProcTestCase</code> records, specifically the <code>{@link FrameProcTestCase#indCase()}</code> fields.
     * The natural ordering of the index values is applied; specifically, the lowest index will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned comparator <b>returns 0</b> if the two index fields are equal.
     * In the current implementation the <code>{@link #indCase()}</code> field will always be unique for each
     * <code>FrameProcTestCase</code> instance.  Thus, the only possibility of equal index fields is equal
     * record instances.  Thus, the same test case can never appear in a Java collection asserting uniqueness.   
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by test case index
     */
    public static Comparator<FrameProcTestCase>   caseIndexOrdering() {
        
        Comparator<FrameProcTestCase> cmp = (r1, r2) -> {
            if (r1.indCase() < r2.indCase())
                return -1;
            else if (r1.indCase == r2.indCase)
                return 0;
            else
                return +1;
        };
        
        return cmp;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given <code>IngestionFrameProcessor</code> instance according to the parameters of this test case.
     * </p>
     * 
     * @param supplier the ingestion frame supplier under evaluation
     * 
     * @return  a record containing the results of the evaluations
     */
    public FrameProcTestResult    evaluate(IngestionFrameProcessor processor) /* throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException, InterruptedException */ {
        
        // Create the processor payload a priori
        List<IngestionFrame>    lstFrames;
        try {
            lstFrames = this.createPayload();   // throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException
            
        } catch (Exception e) {
            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Failed to created payload.", e);
            
            return FrameProcTestResult.from(recFail, this);
        }
        long    szAllocRaw = lstFrames.stream().mapToLong(frm -> frm.allocationSizeFrame()).sum();
        
        // Configure processor and message consumer task
        this.recPrcrCfg.configure(processor);
        MessageConsumer thrdMsgSnk = MessageConsumer.from(processor);
        
        // Initialize the processor and consumer task 
        try {
            processor.activate();       // throws RejectedExecutionException
            thrdMsgSnk.start();          // throws IllegalThreadStateException
        
        } catch (RejectedExecutionException e) {
            processor.shutdownNow();
            
            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Process activation failed.", e);
            
            return FrameProcTestResult.from(recFail, this);
            
        } catch (IllegalThreadStateException e) {
            processor.shutdownNow();
            thrdMsgSnk.terminate();

            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Message consumer task already started.", e);
            
            return FrameProcTestResult.from(recFail, this);
        }
        
        // Perform the processor evaluations
        try {
            Instant     insStart = Instant.now();
            processor.submit(lstFrames);                // throws IllegalStateException
            processor.shutdown();                       // throws InterruptedException
            thrdMsgSnk.join();                          // throws InterruptedException
            Instant     insFinish = Instant.now();
            
            // Collect results and return them
            ResultStatus    recTestStatus = thrdMsgSnk.getResult();
            int             cntMsgs = thrdMsgSnk.getMessageCount();
            long            szAllocProc = thrdMsgSnk.getAllocation();
            Duration        durProcess = Duration.between(insStart, insFinish);
            double          dblRateRaw = ((double)(szAllocRaw * 1_000))/durProcess.toNanos();
            double          dblRateProc = ((double)(szAllocProc * 1_000))/durProcess.toNanos();

            FrameProcTestResult recResult = FrameProcTestResult.from(recTestStatus, this.cntFrames, szAllocRaw, cntMsgs, szAllocProc, durProcess, dblRateRaw, dblRateProc, this);

            return recResult;

        } catch (Exception e) {
            processor.shutdownNow();
            thrdMsgSnk.terminate();

            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Processor evaluation failure during execution.", e);
            
            return FrameProcTestResult.from(recFail, this);
        }
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
        if (strPad == null)
            strPad = "";
        String strPadd = strPad + "  ";
        String strPaddd = strPadd + "  ";
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase);
        ps.println(strPadd + "IngestionFrameProcessor Configuration");
        this.recPrcrCfg.printOut(ps, strPaddd);
        ps.println(strPadd + "Payload Configuraiton");
        ps.println(strPaddd + "Ingestion frame count      : " + this.cntFrames);
        this.specFrame.printOut(ps, strPaddd);
    }
    
    
    //
    // Comparable<FrameProcTestCase> Interface
    //
    
    /**
     * <p>
     * Provides a comparison of <code>FrameProcTestCase</code> records based upon the test case index.
     * </p>
     * <p>
     * This comparison provides a natural ordering of <code>FrameProcTestCase</code> records by test case
     * index <code>{@link #indCase()}</code>.  Specifically, records with the lowest index will appear first
     * in any ordering.  No other fields are compared.
     * Records with equal <code>{@link #indCase()}</code> fields will return zero.
     * </p> 
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FrameProcTestCase recCase) {
        
        if (this.indCase < recCase.indCase)
            return -1;
        else if (this.indCase == recCase.indCase)
            return 0;
        else
            return +1;
    }
    
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns the <code>IngestionFrame</code> payload for the supplier evaluation.
     * </p>
     * <p>
     * Instantiates a new <code>IngestionFrameGeneratorDeprecated</code> object configured for <code>IngestionGrame</code>
     * building with the <code>{@link #recFrmCfg}</code> field.  Then <code>{@link #cntFrames}</code> 
     * <code>IngestionFrame</code> objects are built and used to populate the returned collection.
     * </p>
     * 
     * @return  a new payload of ingestion frames according to the frame count and frame generator configuration
     *  
     * @throws IllegalArgumentException     the sample count and/or the sample period was non-positive 
     * @throws IllegalStateException        there are no data columns defined in frame factory
     * @throws DateTimeException            internal <code>Instant</code> addition failed (timestamps)
     * @throws ArithmeticException          numeric overflow in <code>Instant</code> addition (timestamps)
     * @throws UnsupportedOperationException an unsupported timestamp case was encountered
     */
    private List<IngestionFrame>    createPayload() throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException {
        
        IFrameFactory           facFrames = this.specFrame.newFactory();
        
        List<IngestionFrame>    lstFrames = new ArrayList<>(this.cntFrames);
        
        for (int iFrm=0; iFrm<this.cntFrames; iFrm++) {
            IngestionFrame  frm = facFrames.nextFrame();    // throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException
            
            lstFrames.add(frm);
        }
            
        return lstFrames;
    }

//    /**
//     * <p>
//     * Configures the given supplier to the conditions of the this test case.
//     * </p>
//     *  
//     * @param supplier supplier to be configured
//     */
//    private void    configureProcessor(IngestionFrameProcessor supplier) {
//
//        this.recPrcrCfg.configure(supplier);
//    }
}
