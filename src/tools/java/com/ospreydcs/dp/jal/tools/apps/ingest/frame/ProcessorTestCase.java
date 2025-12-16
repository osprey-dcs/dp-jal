/*
 * Project: dp-jal
 * File:	ProcessorTestCase.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	ProcessorTestCase
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameGeneratorDeprecated;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.SampleBlockConfigDep;

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
 * @param   recFrmCfg   configuration record for <code>IngestionFrame</code> builder
 * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
 *
 * @author Christopher K. Allen
 * @since Sep 15, 2025
 *
 */
public record ProcessorTestCase(
        int                     indCase,
        int                     cntFrames,
        SampleBlockConfigDep       recFrmCfg,
        FrameProcessorConfig    recPrcrCfg
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ProcessorTestCase</code> record populated from the given arguments.
     * </p>
     * <p>
     * The <code>{@link #indCase}</code> field is assigned with the current value of <code>{@link #IND_CASE}</code>
     * which is allows incremented in the canonical constructor.
     * </p>
     * 
     * @param   cntFrame    number of <code>IngestionFrame</code> instances composing test payload
     * @param   recFrmCfg   configuration record for <code>IngestionFrame</code> builder
     * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     * 
     * @return  a new <code>ProcessorTestCase</code> record with fields populated with the given arguments
     */
    public static ProcessorTestCase from(int cntFrames, SampleBlockConfigDep recFrmCfg, FrameProcessorConfig recPrcrCfg) {
        return new ProcessorTestCase(IND_CASE, cntFrames, recFrmCfg, recPrcrCfg);
    }

    
    //
    // Record Types
    //
    
    public static class MessageConsumer extends Thread {

        //
        // Defining Attributes
        //
        
        private final IngestionFrameProcessor   processor;
        
        
        //
        // State Variables
        //
        
        private int     cntMsgs = 0;
        
        private boolean bolStart = false;
        
        private boolean bolFinish = false;
        
        private boolean bolTerminate = false;
        
        private ResultStatus    recStatus;
        
        
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>MessageConsumer</code> instance attached to the given ingestion frame processor.
         * </p>
         *
         * @param processor ingestion frame processor producing <code>IngestDataRequest</code> messages
         */
        public MessageConsumer(IngestionFrameProcessor processor) {
            this.processor = processor;
        }
        
        //
        // Thread Overrides
        //
        
        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            
            while (processor.isSupplying() && !this.bolTerminate) {
                
                try {
                    IngestDataRequest   msgRqst = processor.take();
                    
                    
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
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
     * Constructs a new <code>ProcessorTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor. Sets field values then increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param   indCase     test case index
     * 
     * @param   cntFrame    number of <code>IngestionFrame</code> instances composing test payload
     * @param   recFrmCfg   configuration record for <code>IngestionFrame</code> builder
     * @param   recPrcrCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     */
    public ProcessorTestCase {
        IND_CASE++;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given <code>IngestionFrameProcessor</code> instance according to the parameters of this test case.
     * </p>
     * 
     * @param processor the ingestion frame processor under evaluation
     * 
     * @return  a record containing the results of the evaluations
     */
    public ProcessorTestResult    evaluate(IngestionFrameProcessor processor) {
        
        this.recPrcrCfg.configureProcessor(processor);
        
        List<IngestionFrame>    lstFrames = this.createPayload();
        
        processor.submit(lstFrames);
        
        // TODO - Finish implementation
        ProcessorTestResult recResult = new ProcessorTestResult(
                ResultStatus.newFailure("Not Implementated"),
                0,
                0L,
                0,
                0L,
                Duration.ZERO,
                0.0,
                this
                );
        
        return recResult;
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
        ps.println(strPadd + "Payload Configuraiton");
        ps.println(strPaddd + "Ingestion frame count    : " + this.cntFrames);
        this.recFrmCfg.printOut(ps, strPaddd);
        ps.println(strPadd + "IngestionFrameProcessor Configuration");
        this.recPrcrCfg.printOut(ps, strPaddd);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns the <code>IngestionFrame</code> payload for the processor evaluation.
     * </p>
     * <p>
     * Instantiates a new <code>IngestionFrameGeneratorDeprecated</code> object configured for <code>IngestionGrame</code>
     * building with the <code>{@link #recFrmCfg}</code> field.  Then <code>{@link #cntFrames}</code> 
     * <code>IngestionFrame</code> objects are built and used to populate the returned collection.
     * </p>
     * 
     * @return  a new payload of ingestion frames according to the frame count and frame generator configuration 
     */
    private List<IngestionFrame>    createPayload() {
        
        IngestionFrameGeneratorDeprecated     generator = IngestionFrameGeneratorDeprecated.from(this.recFrmCfg());
        
        List<IngestionFrame>    lstFrames = new ArrayList<>(this.cntFrames);
        
        for (int iFrm=0; iFrm<this.cntFrames; iFrm++) {
            IngestionFrame  frm = generator.build();
            
            lstFrames.add(frm);
        }
            
        return lstFrames;
    }
    
    /**
     * <p>
     * Configures the given processor to the conditions of the this test case.
     * </p>
     *  
     * @param processor processor to be configured
     */
    private void    configureProcessor(IngestionFrameProcessor processor) {

        this.recPrcrCfg.configureProcessor(processor);
    }
    

}
