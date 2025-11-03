/*
 * Project: dp-jal
 * File:	ProcessorTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	ProcessorTestResult
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

import java.time.Duration;

import com.ospreydcs.dp.jal.common.ResultStatus;

/**
 * 
 *
 * @author Christopher K. Allen
 * @since Sep 15, 2025
 *
 */
public record ProcessorTestResult(
        ResultStatus        recTestStatus,
        
        int                 cntRspMsgs,
        long                szRspMsgs,
        int                 cntCorrelSet,
        long                szProcessed,
        Duration            durProcessed,
        double              dblDataRate,
        
        ProcessorTestCase  recTestCase
        ) 
{

}
