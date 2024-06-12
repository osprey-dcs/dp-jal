/*
 * Project: dp-api-common
 * File:	PvMetaRecord.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	PvMetaRecord
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
 * @since Mar 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.time.Instant;

/**
 * <p>
 * Record containing metadata for a single data source, or Process Variable (PV).
 * </p>
 * <p>
 * A Java record class containing the metadata information for a single process variable, as maintained by
 * the Data Platform and available through the Query Service.  Typically these records are provided within results
 * sets of metadata queries concerning process variable properties.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>In a consistent Data Archive a process variable should ALWAYS produce data of the same type.</li>
 * <li>If the process variable did not utilize a sampling clock the <code>{@link lastClock}</code> parameter 
 *     is <code>null</code>.</li>
 * <li>The <code>firstTimestamp</code> parameter is the first instant the process variable is seen in the 
 *     Data Archive.</li>
 * <li>The <code>lastTimestamp</code> parameter is the last instant the process variable is seen in the Data Archive.</li> 
 * </ul>
 * </p>
 * 
 * @param   name            data source name
 * @param   lastType        the type of data the data source last produced
 * @param   lastClock       the sampling clock for last used by the data source
 * @param   firstTimestamp  the timestamp where the data source was first active (i.e., earliest archive timestamp) 
 * @param   lastTimestamp   the timestamp where the data source was last active (i.e., latest archive timestamp)
 * 
 * @author Christopher K. Allen
 * @since Mar 14, 2024
 *
 */
public record PvMetaRecord(
        String                  name,
        DpSupportedType         lastType,
        UniformSamplingClock    lastClock,
        Instant                 firstTimestamp,
        Instant                 lastTimestamp
        ) 
{

}
